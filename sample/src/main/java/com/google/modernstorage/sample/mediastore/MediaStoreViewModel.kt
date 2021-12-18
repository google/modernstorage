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
import android.os.Build
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
    enum class MediaType {
        IMAGE, VIDEO, AUDIO
    }

    fun addMedia(type: MediaType) {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                return@launch
            }

            val extension = when (type) {
                MediaType.IMAGE -> "jpg"
                MediaType.VIDEO -> "mp4"
                MediaType.AUDIO -> "wav"
            }

            val mimeType = when (type) {
                MediaType.IMAGE -> "image/jpeg"
                MediaType.VIDEO -> "video/mp4"
                MediaType.AUDIO -> "audio/wav"
            }

            val collection = when (type) {
                MediaType.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                MediaType.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val uri = fileSystem.createMediaStoreUri(
                filename = "added-${System.currentTimeMillis()}.$extension",
                collection = collection
            ) ?: return@launch clearAddedFile()

            val path = uri.toPath()

            fileSystem.sink(path).buffer().writeAll(context.assets.open("sample.$extension").source())
            fileSystem.scanUri(uri, mimeType)

            val metadata = fileSystem.metadataOrNull(path) ?: return@launch clearAddedFile()
            _addedFile.value = FileDetails(uri, path, metadata)
        }
    }

    enum class DocumentType {
        TEXT, PDF, ZIP
    }

    fun addDocument(type: DocumentType) {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                return@launch
            }

            val extension = when (type) {
                DocumentType.TEXT -> "txt"
                DocumentType.PDF -> "pdf"
                DocumentType.ZIP -> "zip"
            }

            val mimeType = when (type) {
                DocumentType.TEXT -> "text/plain"
                DocumentType.PDF -> "application/pdf"
                DocumentType.ZIP -> "application/zip"
            }

            val uri = fileSystem.createMediaStoreUri(
                filename = "added-${System.currentTimeMillis()}.$extension",
                collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            ) ?: return@launch clearAddedFile()

            val path = uri.toPath()

            fileSystem.sink(path).buffer().writeAll(context.assets.open("sample.$extension").source())
            fileSystem.scanUri(uri, mimeType)

            val metadata = fileSystem.metadataOrNull(path) ?: return@launch clearAddedFile()
            _addedFile.value = FileDetails(uri, path, metadata)
        }
    }
}
