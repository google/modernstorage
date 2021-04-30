package com.google.modernstorage.sample.mediastore

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.google.modernstorage.media.MediaResource
import com.google.modernstorage.media.MediaStoreClient
import com.google.modernstorage.media.SharedPrimary
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
    private val mediaStore: MediaStoreClient by lazy { MediaStoreClient(application) }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun setLoadingStatus(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    val _currentMedia: LiveData<MediaResource?> = MutableLiveData(null)
    val currentMedia: LiveData<MediaResource?> = savedStateHandle.getLiveData<MediaResource?>("currentMedia")

    fun setCurrentMedia(uri: Uri) {
        viewModelScope.launch {
            mediaStore.getResourceByUri(uri)?.let {
                savedStateHandle.set("currentMediaUri", uri)
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