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
package com.google.modernstorage.mediastore

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * MediaStore client providing an easier way to interact with it.
 *
 * @constructor Creates a [MediaStoreRepository] with a [Context]
 */
class MediaStoreRepository(private val context: Context) {

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    /**
     * Returns true if the current [Context] can read MediaStore entries it created.
     */
    fun canReadOwnEntries(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Returns true if the current [Context] can create entries in MediaStore.
     */
    fun canWriteOwnEntries(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Returns true if the current [Context] can read MediaStore entries created by other apps.
     */
    fun canReadSharedEntries(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns true if the current [Context] can edit or delete MediaStore entries created by other
     * apps.
     */
    fun canWriteSharedEntries(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            canReadSharedEntries()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getDisplayNameColumn(type: FileType) = when (type) {
        FileType.IMAGE -> MediaStore.Images.ImageColumns.DISPLAY_NAME
        FileType.AUDIO -> MediaStore.Audio.AudioColumns.DISPLAY_NAME
        FileType.VIDEO -> MediaStore.Video.VideoColumns.DISPLAY_NAME
        else -> throw IllegalArgumentException("Unsupported kind: $type")
    }

    /**
     * Create a [MediaStore] [Uri] and returns it if successful otherwise returns `null`.
     *
     * @param filename preferred filename for the media entry. System can add a suffix if there's
     * already a file with the same name. Some devices can trim the filename when special characters
     * are used.
     * @param location storage volume where the media file will be saved.
     */
    suspend fun createMediaUri(
        filename: String,
        type: FileType,
        location: StorageLocation,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): Result<Uri> = scope.async {
        val entry = ContentValues().apply {
            put(getDisplayNameColumn(type), filename)
        }

        val collection = when (type) {
            FileType.IMAGE -> getImageCollection(location)
            FileType.AUDIO -> getAudioCollection(location)
            FileType.VIDEO -> getVideoCollection(location)
            else -> {
                return@async Result.failure(IllegalArgumentException("Unsupported kind: $type"))
            }
        }

        val uri = contentResolver.insert(collection, entry)
            ?: return@async Result.failure(Exceptions.uriNotCreatedException(filename))

        return@async Result.success(uri)
    }.await()

    private suspend fun getPathByUri(
        uri: Uri,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): Result<String> = scope.async {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(FileColumns.DATA),
            null,
            null,
            null
        ) ?: return@async Result.failure(Exceptions.uriNotFoundException(uri))

        cursor.use {
            if (!cursor.moveToFirst()) {
                return@async Result.failure(Exceptions.uriNotFoundException(uri))
            }

            return@async Result.success(cursor.getString(cursor.getColumnIndexOrThrow(FileColumns.DATA)))
        }
    }.await()

    /**
     * Add a media file in [MediaStore] from an [InputStream] and returns its content [Uri].
     *
     * The file will be scanned by MediaScanner once its content is saved and returns its [Uri],
     * even if the scan fails.
     *
     * @param filename preferred filename for the media entry. System can add a suffix if there's
     * already a file with the same name. Some devices can trim the filename when special characters
     * are used.
     * @param type media type content.
     * @param inputStream media content.
     * @param location storage volume where the image will be saved.
     */
    suspend fun addMediaFromStream(
        filename: String,
        type: FileType,
        mimeType: String,
        inputStream: InputStream,
        location: StorageLocation,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): Result<Uri> = scope.async {
        val uri = createMediaUri(filename, type, location).getOrElse {
            return@async Result.failure(it)
        }

        contentResolver.openOutputStream(uri, "w").use { outputStream ->
            if (outputStream == null) {
                return@async Result.failure(Exceptions.unopenableOutputStreamException(uri))
            } else {
                inputStream.copyTo(outputStream)
            }
        }

        val path = getPathByUri(uri).getOrNull() ?: return@async Result.success(uri)
        scanFilePath(path = path, mimeType = mimeType, scope = scope)

        return@async Result.success(uri)
    }.await()

    /**
     * When adding a file (using java.io or ContentResolver APIs), MediaStore might not be aware of
     * the new entry or doesn't have an updated version of it. That's why some entries have 0 bytes
     * size, even though the file is definitely not empty. MediaStore will eventually scan the file
     * but it's better to do it ourselves to have a fresher state whenever we can.
     */
    private suspend fun scanFilePath(
        path: String,
        mimeType: String,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): Uri {
        return suspendCoroutine { continuation ->
            scope.launch {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(path),
                    arrayOf(mimeType)
                ) { _, uri ->
                    continuation.resume(uri)
                }
            }
        }
    }

    /**
     * When adding a file (using java.io or ContentResolver APIs), MediaStore might not be aware of
     * the new entry or doesn't have an updated version of it. That's why some entries have 0 bytes
     * size, even though the file is definitely not empty. MediaStore will eventually scan the file
     * but it's better to do it ourselves to have a fresher state whenever we can.
     */
    suspend fun scanUri(
        uri: Uri,
        mimeType: String,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): Result<Boolean> {
        return suspendCoroutine { continuation ->
            scope.launch {
                val cursor = context.contentResolver.query(
                    uri,
                    arrayOf(FileColumns.DATA),
                    null,
                    null,
                    null
                ) ?: return@launch continuation.resume(
                    Result.failure(
                        Exceptions.uriNotFoundException(
                            uri
                        )
                    )
                )

                val path = cursor.use {
                    if (!cursor.moveToFirst()) {
                        return@launch continuation.resume(
                            Result.failure(
                                Exceptions.uriNotFoundException(
                                    uri
                                )
                            )
                        )
                    }

                    return@use cursor.getString(cursor.getColumnIndexOrThrow(FileColumns.DATA))
                }

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(path),
                    arrayOf(mimeType)
                ) { _, scannedUri ->
                    if (scannedUri != null) {
                        continuation.resume(Result.success(true))
                    } else {
                        continuation.resume(Result.failure(Exceptions.uriNotScannedException(uri)))
                    }
                }
            }
        }
    }

    /**
     * Returns a [FileResource] if it finds its [Uri] in MediaStore otherwise returns `null`.
     *
     * @param mediaUri [Uri] representing the MediaStore entry.
     */
    suspend fun getResourceByUri(
        mediaUri: Uri,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): Result<FileResource> = scope.async {
        val projection = arrayOf(
            FileColumns.DISPLAY_NAME,
            FileColumns.SIZE,
//        FileColumns.MEDIA_TYPE,
            FileColumns.MIME_TYPE,
        )

        val cursor = contentResolver.query(
            mediaUri,
            projection,
            null,
            null,
            null
        ) ?: return@async Result.failure(Exceptions.uriNotFoundException(mediaUri))

        cursor.use {
            if (!cursor.moveToFirst()) {
                return@async Result.failure(Exceptions.uriNotFoundException(mediaUri))
            }

            val displayNameColumn = cursor.getColumnIndexOrThrow(FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(FileColumns.SIZE)
//            val mediaTypeColumn = cursor.getColumnIndexOrThrow(FileColumns.MEDIA_TYPE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(FileColumns.MIME_TYPE)

            return@async Result.success(
                FileResource(
                    uri = mediaUri,
                    filename = cursor.getString(displayNameColumn),
                    size = cursor.getLong(sizeColumn),
//                type = MediaType.getEnum(cursor.getInt(mediaTypeColumn)),
                    type = FileType.IMAGE,
                    mimeType = cursor.getString(mimeTypeColumn),
                )
            )
        }
    }.await()

    companion object {
        /**
         * Returns the [MediaStore] Image collection based on the provided [StorageLocation].
         *
         * @param location Location representing a storage volume.
         */
        fun getImageCollection(location: StorageLocation): Uri {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (location) {
                    Internal -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_INTERNAL)
                    SharedPrimary -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }
            } else {
                when (location) {
                    Internal -> MediaStore.Images.Media.INTERNAL_CONTENT_URI
                    SharedPrimary -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            }
        }

        /**
         * Returns the [MediaStore] Video collection based on the provided [StorageLocation].
         *
         * @param location Location representing a storage volume.
         */
        fun getVideoCollection(location: StorageLocation): Uri {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (location) {
                    Internal -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_INTERNAL)
                    SharedPrimary -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }
            } else {
                when (location) {
                    Internal -> MediaStore.Video.Media.INTERNAL_CONTENT_URI
                    SharedPrimary -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
            }
        }

        /**
         * Returns the [MediaStore] Audio collection based on the provided [StorageLocation].
         *
         * @param location Location representing a storage volume.
         */
        fun getAudioCollection(location: StorageLocation): Uri {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (location) {
                    Internal -> MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_INTERNAL)
                    SharedPrimary -> MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }
            } else {
                when (location) {
                    Internal -> MediaStore.Audio.Media.INTERNAL_CONTENT_URI
                    SharedPrimary -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }
        }
    }
}
