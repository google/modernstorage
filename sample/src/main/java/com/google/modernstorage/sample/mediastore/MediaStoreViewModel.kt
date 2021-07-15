/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.modernstorage.sample.mediastore

import android.app.Application
import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.modernstorage.mediastore.FileResource
import com.google.modernstorage.mediastore.FileType
import com.google.modernstorage.mediastore.MediaStoreRepository
import com.google.modernstorage.mediastore.SharedPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import okhttp3.OkHttpClient
import okhttp3.Request

const val CURRENT_MEDIA_KEY = "currentMedia"
const val TEMPORARY_CAMERA_IMAGE_URI_KEY = "temporaryCameraImageUri"

class MediaStoreViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val httpClient by lazy { OkHttpClient() }
    private val mediaStore by lazy { MediaStoreRepository(application) }

    val canWriteInMediaStore: Boolean
        get() = mediaStore.canWriteOwnEntries()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private fun setLoadingStatus(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }

    private val _snackbarNotification: MutableLiveData<String> = MutableLiveData()
    val snackbarNotification: LiveData<String> get() = _snackbarNotification

    fun clearSnackbarNotificationState() {
        _snackbarNotification.postValue(null)
    }

    @Parcelize
    data class CaptureMediaIntentRequest(val type: FileType, val uri: Uri) : Parcelable

    private val _captureMediaIntent: MutableLiveData<CaptureMediaIntentRequest> = MutableLiveData()
    val captureMediaIntent: LiveData<CaptureMediaIntentRequest> get() = _captureMediaIntent

    private fun setCaptureMediaIntentRequest(type: FileType, uri: Uri) {
        _captureMediaIntent.postValue(CaptureMediaIntentRequest(type, uri))
    }

    fun clearCaptureMediaIntentRequest() {
        _captureMediaIntent.postValue(null)
    }

    val currentFile: LiveData<FileResource> = savedStateHandle.getLiveData(CURRENT_MEDIA_KEY)

    private suspend fun setCurrentMedia(uri: Uri) {
        savedStateHandle.set(CURRENT_MEDIA_KEY, mediaStore.getResourceByUri(uri).getOrNull())
    }

    private val temporaryCameraImageUri: Uri?
        get() = savedStateHandle.get(TEMPORARY_CAMERA_IMAGE_URI_KEY)

    fun saveTemporaryCameraImageUri(uri: Uri) {
        savedStateHandle.set(TEMPORARY_CAMERA_IMAGE_URI_KEY, uri)
    }

    private fun clearTemporaryCameraImageUri() {
        savedStateHandle.remove<Uri>(TEMPORARY_CAMERA_IMAGE_URI_KEY)
    }

    enum class MediaSource {
        CAMERA, INTERNET
    }

    fun saveRandomMediaFromInternet(type: FileType) {
        setLoadingStatus(true)

        val url: String
        val extension: String
        val mimeType: String

        when (type) {
            FileType.IMAGE -> {
                url = SampleData.image.random()
                extension = "jpg"
                mimeType = "image/jpeg"
            }
            FileType.VIDEO -> {
                url = SampleData.video.random()
                extension = "mp4"
                mimeType = "video/mp4"
            }
            else -> throw IllegalArgumentException("Unsupported type: $type")
        }

        viewModelScope.launch {
            val request = Request.Builder().url(url).build()

            val mediaUri = withContext(Dispatchers.IO) {
                @Suppress("BlockingMethodInNonBlockingContext")
                val response = httpClient.newCall(request).execute()

                response.body?.use { responseBody ->
                    val filename = generateFilename(MediaSource.INTERNET, extension)

                    return@withContext mediaStore.addMediaFromStream(
                        filename = filename,
                        type = type,
                        mimeType = mimeType,
                        inputStream = responseBody.byteStream(),
                        location = SharedPrimary
                    )
                } ?: return@withContext Result.failure(Exception("Could not download this media"))
            }.getOrElse {
                setLoadingStatus(false)
                return@launch _snackbarNotification.postValue(it.message)
            }

            mediaStore.getResourceByUri(mediaUri)
                .onSuccess {
                    savedStateHandle.set(CURRENT_MEDIA_KEY, it)
                    setLoadingStatus(false)
                }
                .onFailure {
                    savedStateHandle.set(CURRENT_MEDIA_KEY, null)
                    setLoadingStatus(false)
                    return@launch _snackbarNotification.postValue(it.message)
                }
        }
    }

    fun captureMedia(mediaType: FileType) {
        viewModelScope.launch {
            val uri = createMediaUriForCamera(mediaType).getOrElse {
                return@launch _snackbarNotification.postValue(it.message)
            }

            createMediaUriForCamera(mediaType)
                .onSuccess {
                    when (mediaType) {
                        FileType.IMAGE -> {
                            savedStateHandle.set(TEMPORARY_CAMERA_IMAGE_URI_KEY, uri)
                            setCaptureMediaIntentRequest(FileType.IMAGE, uri)
                        }
                        FileType.VIDEO -> {
                            setCaptureMediaIntentRequest(FileType.VIDEO, uri)
                        }
                        else -> throw IllegalArgumentException("Unsupported type: $mediaType")
                    }
                }
                .onFailure {
                    return@launch _snackbarNotification.postValue(it.message)
                }
        }
    }

    private fun generateFilename(source: MediaSource, extension: String): String {
        return when (source) {
            MediaSource.CAMERA -> "camera-${System.currentTimeMillis()}.$extension"
            MediaSource.INTERNET -> "internet-${System.currentTimeMillis()}.$extension"
        }
    }

    private suspend fun createMediaUriForCamera(type: FileType): Result<Uri> {
        return when (type) {
            FileType.IMAGE -> mediaStore.createMediaUri(
                filename = generateFilename(MediaSource.CAMERA, "jpg"),
                type = FileType.IMAGE,
                location = SharedPrimary
            )
            FileType.VIDEO -> mediaStore.createMediaUri(
                generateFilename(MediaSource.CAMERA, "mp4"),
                type = FileType.VIDEO,
                SharedPrimary
            )
            else -> throw IllegalArgumentException("Unsupported type: $type")
        }
    }

    fun onPhotoCapture(success: Boolean) {
        setLoadingStatus(false)

        if (!success) {
            _snackbarNotification.postValue("Image capture failed")
            return
        }

        if (temporaryCameraImageUri == null) {
            _snackbarNotification.postValue("Can't find previously saved temporary Camera Image URI")
        } else {
            viewModelScope.launch {
                setCurrentMedia(temporaryCameraImageUri!!)
                clearTemporaryCameraImageUri()
            }
        }
    }

    fun onVideoCapture(uri: Uri?) {
        setLoadingStatus(false)

        if (uri == null) {
            _snackbarNotification.postValue("Video capture failed")
            return
        }

        viewModelScope.launch {
            setCurrentMedia(uri)
        }
    }
}
