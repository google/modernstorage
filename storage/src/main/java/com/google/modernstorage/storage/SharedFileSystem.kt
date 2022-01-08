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
package com.google.modernstorage.storage

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source
import okio.sink
import okio.source
import java.io.IOException
import kotlin.coroutines.resume

class SharedFileSystem(private val context: Context) : FileSystem() {
    private val contentResolver = context.contentResolver

    /**
     * Not yet implemented
     */
    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        TODO("Not yet implemented")
    }

    /**
     * Not yet implemented
     */
    override fun atomicMove(source: Path, target: Path) {
        TODO("Not yet implemented")
    }

    override fun canonicalize(path: Path): Path {
        throw UnsupportedOperationException("Paths can't be canonicalized in SharedFileSystem")
    }

    /**
     * Not yet implemented
     */
    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        TODO("Not yet implemented")
    }

    override fun createSymlink(source: Path, target: Path) {
        throw UnsupportedOperationException("Symlinks  can't be created in SharedFileSystem")
    }

    override fun delete(path: Path, mustExist: Boolean) {
        TODO("Not yet implemented")
    }

    override fun list(dir: Path): List<Path> = list(dir, throwOnFailure = true)!!

    override fun listOrNull(dir: Path): List<Path>? = list(dir, throwOnFailure = false)

    private fun list(dir: Path, throwOnFailure: Boolean): List<Path>? {
        // TODO: Verify path is a directory
        val rootUri = dir.toUri()
        val documentId = DocumentsContract.getDocumentId(rootUri)
        val treeUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, documentId)

        val cursor = contentResolver.query(
            treeUri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
            null,
            null,
            null,
            null
        )

        if (cursor == null) {
            if (throwOnFailure) {
                throw IOException("failed to list $dir")
            } else {
                return null
            }
        }

        val result = mutableListOf<Path>()

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                result.add(DocumentsContract.buildDocumentUriUsingTree(rootUri, documentId).toPath())
            }
        }

        return result
    }

    override fun metadataOrNull(path: Path): FileMetadata? {
        val uri = path.toUri()

        return when (uri.authority) {
            MediaStore.AUTHORITY -> fetchMetadataFromMediaStore(path, uri)
            else -> fetchMetadataFromDocumentProvider(path, uri)
        }
    }

    private fun fetchMetadataFromMediaStore(path: Path, uri: Uri): FileMetadata? {
        val cursor = contentResolver.query(
            uri,
            arrayOf(
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATA,
            ),
            null,
            null,
            null
        ) ?: return null

        cursor.use { cursor ->
            if (!cursor.moveToNext()) {
                return null
            }

            // FileColumns.DATE_MODIFIED
            val lastModifiedTime = cursor.getLong(0)
            // FileColumns.DISPLAY_NAME
            val displayName = cursor.getString(1)
            // FileColumns.MIME_TYPE
            val mimeType = cursor.getString(2)
            // FileColumns.SIZE
            val size = cursor.getLong(3)
            // FileColumns.DATA
            val filePath = cursor.getString(4)

            return FileMetadata(
                isRegularFile = true,
                isDirectory = false,
                symlinkTarget = null,
                size = size,
                createdAtMillis = null,
                lastModifiedAtMillis = lastModifiedTime,
                lastAccessedAtMillis = null,
                extras = mapOf(
                    Path::class to path,
                    Uri::class to uri,
                    MetadataExtras.DisplayName::class to MetadataExtras.DisplayName(displayName),
                    MetadataExtras.MimeType::class to MetadataExtras.MimeType(mimeType),
                    MetadataExtras.FilePath::class to MetadataExtras.FilePath(filePath),
                )
            )
        }
    }

    private fun fetchMetadataFromDocumentProvider(path: Path, uri: Uri): FileMetadata? {
        val cursor = contentResolver.query(
            uri,
            arrayOf(
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE
            ),
            null,
            null,
            null
        ) ?: return null

        cursor.use { cursor ->
            if (!cursor.moveToNext()) {
                return null
            }

            // DocumentsContract.Document.COLUMN_LAST_MODIFIED
            val lastModifiedTime = cursor.getLong(0)
            // DocumentsContract.Document.COLUMN_DISPLAY_NAME
            val displayName = cursor.getString(1)
            // DocumentsContract.Document.COLUMN_MIME_TYPE
            val mimeType = cursor.getString(2)
            // DocumentsContract.Document.COLUMN_SIZE
            val size = cursor.getLong(3)

            val isFolder = mimeType == DocumentsContract.Document.MIME_TYPE_DIR ||
                mimeType == DocumentsContract.Root.MIME_TYPE_ITEM

            return FileMetadata(
                isRegularFile = !isFolder,
                isDirectory = isFolder,
                symlinkTarget = null,
                size = size,
                createdAtMillis = null,
                lastModifiedAtMillis = lastModifiedTime,
                lastAccessedAtMillis = null,
                extras = mapOf(
                    Path::class to path,
                    Uri::class to uri,
                    MetadataExtras.DisplayName::class to MetadataExtras.DisplayName(displayName),
                    MetadataExtras.MimeType::class to MetadataExtras.MimeType(mimeType),
                )
            )
        }
    }

    /**
     * Not yet implemented
     */
    override fun openReadOnly(file: Path): FileHandle {
        TODO("Not yet implemented")
    }

    /**
     * Not yet implemented
     */
    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        TODO("Not yet implemented")
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        if (mustCreate) {
            throw IOException("Path creation isn't supported ($file)")
        }

        val uri = file.toUri()
        val outputStream = contentResolver.openOutputStream(uri)

        if (outputStream == null) {
            throw IOException("Couldn't open an OutputStream ($file)")
        } else {
            return outputStream.sink()
        }
    }

    override fun source(file: Path): Source {
        val uri = file.toUri()
        val inputStream = contentResolver.openInputStream(uri)

        if (inputStream == null) {
            throw IOException("Couldn't open an InputStream ($file)")
        } else {
            return inputStream.source()
        }
    }

    fun createMediaStoreUri(filename: String, directory: String): Uri? {
        val newEntry = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.DATA, "$directory/$filename")
        }

        val collection = MediaStore.Files.getContentUri("external")
        return context.contentResolver.insert(collection, newEntry)
    }

    suspend fun scanUri(uri: Uri, mimeType: String): Uri? {
        val cursor = contentResolver.query(
            uri,
            arrayOf(MediaStore.Files.FileColumns.DATA),
            null,
            null,
            null
        ) ?: throw Exception("Uri $uri could not be found")

        val path = cursor.use {
            if (!cursor.moveToFirst()) {
                throw Exception("Uri $uri could not be found")
            }

            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
        }

        return suspendCancellableCoroutine { continuation ->
            MediaScannerConnection.scanFile(
                context,
                arrayOf(path),
                arrayOf(mimeType)
            ) { _, scannedUri ->
                if (scannedUri == null) {
                    continuation.cancel(Exception("File $path could not be scanned"))
                } else {
                    continuation.resume(scannedUri)
                }
            }
        }
    }
}
