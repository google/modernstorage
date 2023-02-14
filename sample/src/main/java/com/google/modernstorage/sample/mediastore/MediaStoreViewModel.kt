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
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.modernstorage.sample.ui.shared.FileDetails
import com.google.modernstorage.storage.AndroidFileSystem
import com.google.modernstorage.storage.toOkioPath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okio.source
import java.io.File

class MediaStoreViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication()
    private val fileSystem = AndroidFileSystem(context)

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
            val extension: String
            val mimeType: String
            val collection: Uri
            val directory: File

            when (type) {
                MediaType.IMAGE -> {
                    extension = "jpg"
                    mimeType = "image/jpeg"
                    collection = MediaStore.Images.Media.getContentUri("external")
                    directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                }
                MediaType.VIDEO -> {
                    extension = "mp4"
                    mimeType = "video/mp4"
                    collection = MediaStore.Video.Media.getContentUri("external")
                    directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                }
                MediaType.AUDIO -> {
                    extension = "wav"
                    mimeType = "audio/x-wav"
                    collection = MediaStore.Audio.Media.getContentUri("external")
                    directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                }
            }

            val uri = fileSystem.createMediaStoreUri(
                filename = "added-${System.currentTimeMillis()}.$extension",
                collection = collection,
                directory = directory.absolutePath
            ) ?: return@launch clearAddedFile()

            val path = uri.toOkioPath()

            fileSystem.write(path, false) {
                context.assets.open("sample.$extension").source().use { source ->
                    writeAll(source)
                }
            }
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
            val extension: String
            val mimeType: String

            when (type) {
                DocumentType.TEXT -> {
                    extension = "txt"
                    mimeType = "text/plain"
                }
                DocumentType.PDF -> {
                    extension = "pdf"
                    mimeType = "application/pdf"
                }
                DocumentType.ZIP -> {
                    extension = "zip"
                    mimeType = "application/zip"
                }
            }

            val uri = fileSystem.createMediaStoreUri(
                filename = "added-${System.currentTimeMillis()}.$extension",
                collection = MediaStore.Files.getContentUri("external"),
                directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            )!!

            val path = uri.toOkioPath()

            fileSystem.write(path, false) {
                context.assets.open("sample.$extension").source().use { source ->
                    writeAll(source)
                }
            }
            fileSystem.scanUri(uri, mimeType)

            val metadata = fileSystem.metadataOrNull(path) ?: return@launch clearAddedFile()
            _addedFile.value = FileDetails(uri, path, metadata)
        }
    }
}
