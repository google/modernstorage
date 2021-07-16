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
package com.google.modernstorage.sample.filesystem

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.modernstorage.filesystem.AndroidFileSystems
import com.google.modernstorage.filesystem.AndroidPaths
import com.google.modernstorage.sample.SampleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.file.Files
import java.nio.file.Path

const val CURRENT_DOCUMENT_KEY = "currentDocument"

@RequiresApi(Build.VERSION_CODES.O)
class FileSystemViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val httpClient by lazy { OkHttpClient() }

    val currentFile: LiveData<FileResource> = savedStateHandle.getLiveData(CURRENT_DOCUMENT_KEY)
    private val _currentFileContent = MutableLiveData<FileContent>()
    val currentFileContent: LiveData<FileContent> = _currentFileContent

    init {
        AndroidFileSystems.initialize(application)
    }

    fun previewFile(uri: Uri, type: FileType) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val path = AndroidPaths.get(uri)
                @Suppress("BlockingMethodInNonBlockingContext")
                val file = FileResource(uri, Files.size(path), type)

                withContext(Dispatchers.Main) {
                    savedStateHandle.set(CURRENT_DOCUMENT_KEY, file)
                }

                when (type) {
                    FileType.TEXT -> loadTextContent(path)
                    FileType.IMAGE -> loadImageContent(path)
                }
            }
        }
    }

    private suspend fun loadTextContent(path: Path) {
        withContext(Dispatchers.IO) {
            @Suppress("BlockingMethodInNonBlockingContext")
            val text = Files.readAllLines(path).joinToString(separator = "\n")

            _currentFileContent.postValue(TextContent(text))
        }
    }

    private suspend fun loadImageContent(path: Path) {
        withContext(Dispatchers.IO) {
            @Suppress("BlockingMethodInNonBlockingContext")
            val inputStream = Files.newInputStream(path)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            _currentFileContent.postValue(BitmapContent(bitmap))
        }
    }

    fun downloadAndSaveContent(documentUri: Uri, type: FileType) {
        viewModelScope.launch {
            val path = AndroidPaths.get(documentUri)

            val url = when (type) {
                FileType.TEXT -> SampleData.texts.random()
                FileType.IMAGE -> SampleData.image.random()
            }

            val request = Request.Builder().url(url).build()

            withContext(Dispatchers.IO) {
                @Suppress("BlockingMethodInNonBlockingContext")
                val response = httpClient.newCall(request).execute()

                response.body?.use { responseBody ->
                    @Suppress("BlockingMethodInNonBlockingContext")
                    Files.copy(responseBody.byteStream(), path)
                }
            }

            previewFile(documentUri, type)
        }
    }
}

@Parcelize
data class FileResource(
    val uri: Uri,
    val size: Long,
    val type: FileType
) : Parcelable

enum class FileType {
    TEXT, IMAGE
}

sealed class FileContent
class TextContent(val value: String) : FileContent()
class BitmapContent(val value: Bitmap) : FileContent()
