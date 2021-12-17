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
import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.modernstorage.sample.ui.shared.FileDetails
import com.google.modernstorage.storage.SharedFileSystem
import com.google.modernstorage.storage.toPath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okio.buffer
import okio.source

class MediaStoreViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication()
    private val fileSystem = SharedFileSystem(context)

    private val _addedFile = MutableStateFlow<FileDetails?>(null)
    val addedFile: StateFlow<FileDetails?> = _addedFile

    private fun clearAddedFile() {
        _addedFile.value = null
    }

    fun addImage() {
        viewModelScope.launch {
            val uri = fileSystem.createMediaStoreUri(
                filename = "added-${System.currentTimeMillis()}.jpg",
                collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ) ?: return@launch clearAddedFile()

            val path = uri.toPath()

            fileSystem.sink(path).buffer().writeAll(context.assets.open("sample.jpg").source())
            fileSystem.scanUri(uri, "image/jpeg")

            val metadata = fileSystem.metadataOrNull(path) ?: return@launch clearAddedFile()
            _addedFile.value = FileDetails(uri, path, metadata)
        }
    }

    fun addVideo() {
        viewModelScope.launch {
            val uri = fileSystem.createMediaStoreUri(
                filename = "added-${System.currentTimeMillis()}.mp4",
                collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            ) ?: return@launch clearAddedFile()

            val path = uri.toPath()

            fileSystem.sink(path).buffer().writeAll(context.assets.open("sample.mp4").source())
            fileSystem.scanUri(uri, "video/mp4")

            val metadata = fileSystem.metadataOrNull(path) ?: return@launch clearAddedFile()
            _addedFile.value = FileDetails(uri, path, metadata)
        }
    }
}
