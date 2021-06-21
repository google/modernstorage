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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.modernstorage.mediastore.FileResource
import com.google.modernstorage.mediastore.FileType
import com.google.modernstorage.mediastore.MediaStoreRepository
import com.google.modernstorage.mediastore.SharedPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception

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

    fun setLoadingStatus(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }

    val currentFile: LiveData<FileResource> = savedStateHandle.getLiveData(CURRENT_MEDIA_KEY)

    suspend fun setCurrentMedia(uri: Uri) {
        savedStateHandle.set(CURRENT_MEDIA_KEY, mediaStore.getResourceByUri(uri).getOrNull())
    }

    val temporaryCameraImageUri: Uri?
        get() = savedStateHandle.get(TEMPORARY_CAMERA_IMAGE_URI_KEY)

    fun saveTemporaryCameraImageUri(uri: Uri) {
        savedStateHandle.set(TEMPORARY_CAMERA_IMAGE_URI_KEY, uri)
    }

    fun clearTemporaryCameraImageUri() {
        savedStateHandle.remove<Uri>(TEMPORARY_CAMERA_IMAGE_URI_KEY)
    }

    suspend fun saveRandomMediaFromInternet(type: FileType): Result<FileResource> {
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

        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            val response = httpClient.newCall(request).execute()

            val mediaUri = response.body?.use { responseBody ->
                val filename = generateFilename(MediaSource.INTERNET, extension)

                return@use mediaStore.addMediaFromStream(
                    filename = filename,
                    type = type,
                    mimeType = mimeType,
                    inputStream = responseBody.byteStream(),
                    location = SharedPrimary
                ).getOrElse {
                    return@withContext Result.failure(it)
                }
            } ?: return@withContext Result.failure(Exception("Could not download this media"))

            mediaStore.getResourceByUri(mediaUri)
                .apply {
                    savedStateHandle.set(CURRENT_MEDIA_KEY, this.getOrNull())
                    setLoadingStatus(false)
                }
                .onSuccess { return@withContext Result.success(it) }
                .onFailure { return@withContext Result.failure(it) }
        }
    }

    suspend fun createMediaUriForCamera(type: MediaType): Result<Uri> {
        return when (type) {
            MediaType.IMAGE -> mediaStore.createMediaUri(
                filename = generateFilename(MediaSource.CAMERA, "jpg"),
                type = FileType.IMAGE,
                location = SharedPrimary
            )
            MediaType.VIDEO -> mediaStore.createMediaUri(
                generateFilename(MediaSource.CAMERA, "mp4"),
                type = FileType.IMAGE,
                SharedPrimary
            )
        }
    }
}

enum class MediaType {
    IMAGE, VIDEO
}

enum class MediaSource {
    CAMERA, INTERNET
}

private fun generateFilename(source: MediaSource, extension: String): String {
    return when (source) {
        MediaSource.CAMERA -> "camera-${System.currentTimeMillis()}.$extension"
        MediaSource.INTERNET -> "internet-${System.currentTimeMillis()}.$extension"
    }
}
