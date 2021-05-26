/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import androidx.lifecycle.viewModelScope
import com.google.modernstorage.mediastore.MediaResource
import com.google.modernstorage.mediastore.MediaStoreClient
import com.google.modernstorage.mediastore.SharedPrimary
import com.google.modernstorage.mediastore.canWriteOwnEntriesInMediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MediaStoreViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val httpClient by lazy { OkHttpClient() }
    private val mediaStore by lazy { MediaStoreClient(application) }

    val canWriteInMediaStore: Boolean
        get() = canWriteOwnEntriesInMediaStore(getApplication())

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun setLoadingStatus(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    private val _currentMedia: MutableLiveData<MediaResource?> = MutableLiveData(null)
    val currentMedia: LiveData<MediaResource?> get() = _currentMedia

    init {
        savedStateHandle.get<Uri>("currentMediaUri")?.let { uri ->
            if(canWriteInMediaStore) {
                viewModelScope.launch {
                    _currentMedia.value = mediaStore.getResourceByUri(uri)
                }
            }
        }
    }

    fun setCurrentMedia(uri: Uri) {
        viewModelScope.launch {
            mediaStore.getResourceByUri(uri)?.let {
                savedStateHandle.set("currentMediaUri", uri)
                _currentMedia.value = mediaStore.getResourceByUri(uri)
            }
        }
    }

    val temporaryCameraImageUri: Uri?
        get() = savedStateHandle.get("temporaryCameraImageUri")

    fun saveTemporaryCameraImageUri(uri: Uri) {
        savedStateHandle.set("temporaryCameraImageUri", uri)
    }

    fun clearTemporaryCameraImageUri() {
        savedStateHandle.remove<Uri>("temporaryCameraImageUri")
    }

    fun saveRandomImageFromInternet(callback: (uri: Uri) -> Unit) {
        viewModelScope.launch {
            val request = Request.Builder().url(SampleData.image.random()).build()

            withContext(Dispatchers.IO) {
                val response = httpClient.newCall(request).execute()

                response.body?.use { responseBody ->
                    val filename = generateFilename(MediaSource.INTERNET, "jpg")
                    val imageUri = mediaStore.addImageFromStream(
                        filename = filename,
                        inputStream = responseBody.byteStream(),
                        location = SharedPrimary
                    )

                    withContext(Dispatchers.Main) {
                        callback(imageUri)
                    }
                }
            }
        }
    }

    fun saveRandomVideoFromInternet(callback: (uri: Uri) -> Unit) {
        viewModelScope.launch {
            val request = Request.Builder().url(SampleData.video.random()).build()

            withContext(Dispatchers.IO) {
                val response = httpClient.newCall(request).execute()

                response.body?.use { responseBody ->
                    val filename = generateFilename(MediaSource.INTERNET, "mp4")
                    val videoUri = mediaStore.addVideoFromStream(
                        filename = filename,
                        inputStream = responseBody.byteStream(),
                        location = SharedPrimary
                    )

                    withContext(Dispatchers.Main) {
                        callback(videoUri)
                    }
                }
            }
        }
    }

    fun createMediaUriForCamera(type: MediaType, callback: (uri: Uri) -> Unit) {
        viewModelScope.launch {
            val uri = when (type) {
                MediaType.IMAGE -> mediaStore.createImageUri(
                    generateFilename(MediaSource.CAMERA, "jpg"),
                    SharedPrimary
                )
                MediaType.VIDEO -> mediaStore.createVideoUri(
                    generateFilename(MediaSource.CAMERA, "mp4"),
                    SharedPrimary
                )
            }

            withContext(Dispatchers.Main) {
                if (uri != null) callback(uri)
            }
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