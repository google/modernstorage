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
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.modernstorage.filesystem.AndroidFileSystems
import com.google.modernstorage.filesystem.AndroidPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

const val CURRENT_DOCUMENT_KEY = "currentDocument"

class FileSystemViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val context: Context
        get() = getApplication()

    val currentFile: LiveData<FileResource> = savedStateHandle.getLiveData(CURRENT_DOCUMENT_KEY)
    private val _currentFileContent = MutableLiveData<FileContent>()
    val currentFileContent: LiveData<FileContent> = _currentFileContent

    init {
        AndroidFileSystems.initialize(application)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun onFileSelect(uri: Uri, type: FileType) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val path = AndroidPaths.get(uri)
                val file = FileResource(uri, Files.size(path), FileType.TEXT)

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

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun loadTextContent(path: Path) {
        withContext(Dispatchers.IO) {
            val reader = Files.newBufferedReader(path)
            val text = reader.lines().collect(Collectors.joining("\n"))
            reader.close()

            _currentFileContent.postValue(TextContent(text))
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun loadImageContent(path: Path) {
        withContext(Dispatchers.IO) {
            val inputStream = Files.newInputStream(path)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            _currentFileContent.postValue(BitmapContent(bitmap))
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
