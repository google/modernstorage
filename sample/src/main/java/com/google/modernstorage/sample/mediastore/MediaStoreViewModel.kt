package com.google.modernstorage.sample.mediastore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.modernstorage.media.MediaStoreClient
import com.google.modernstorage.media.SharedPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MediaStoreViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaStore: MediaStoreClient by lazy { MediaStoreClient(application) }

    private val httpClient by lazy { OkHttpClient() }

    fun saveRandomImageFromInternet(callback: () -> Unit) {
        viewModelScope.launch {
            val request = Request.Builder().url(SampleData.image.random()).build()

            withContext(Dispatchers.IO) {
                val response = httpClient.newCall(request).execute()

                response.body?.use { responseBody ->
                    val filename = generateFilename(MediaSource.INTERNET, "jpg")
                    mediaStore.addImageFromStream(
                        filename = filename,
                        inputStream = responseBody.byteStream(),
                        location = SharedPrimary
                    )

                    withContext(Dispatchers.Main) {
                        callback()
                    }
                }
            }
        }
    }

    fun saveRandomVideoFromInternet(callback: () -> Unit) {
        viewModelScope.launch {
            val request = Request.Builder().url(SampleData.video.random()).build()

            withContext(Dispatchers.IO) {
                val response = httpClient.newCall(request).execute()

                response.body?.use { responseBody ->
                    val filename = generateFilename(MediaSource.INTERNET, "mp4")
                    mediaStore.addVideoFromStream(
                        filename = filename,
                        inputStream = responseBody.byteStream(),
                        location = SharedPrimary
                    )

                    withContext(Dispatchers.Main) {
                        callback()
                    }
                }
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