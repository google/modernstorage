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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * MediaStore client providing an easier way to interact with it.
 *
 * @constructor Creates a [MediaStoreRepository] with a [Context]
 */
class MediaStoreRepository(private val appContext: Context) {

    private val contentResolver: ContentResolver
        get() = appContext.contentResolver

    /**
     * Check if the current [Context] can read MediaStore entries it created.
     */
    fun canReadOwnEntries(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if the current [Context] can create entries in MediaStore.
     */
    fun canWriteOwnEntries(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if the current [Context] can read MediaStore entries created by other apps.
     */
    fun canReadSharedEntries(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if the current [Context] can edit or delete MediaStore entries created by other apps.
     */
    fun canWriteSharedEntries(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            canReadSharedEntries()
        } else {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Get the [MediaStore] Image collection based on the provided [StorageLocation].
     *
     * @param location Location representing a storage volume.
     */
    private fun getImageCollection(location: StorageLocation): Uri {
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
     * Get the [MediaStore] Video collection based on the provided [StorageLocation].
     *
     * @param location Location representing a storage volume.
     */
    private fun getVideoCollection(location: StorageLocation): Uri {
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
     * Get the [MediaStore] Audio collection based on the provided [StorageLocation].
     *
     * @param location Location representing a storage volume.
     */
    private fun getAudioCollection(location: StorageLocation): Uri {
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

    /**
     * Get display name column in [MediaStore] based on the provided [FileType].
     *
     * @param type type of the media file.
     */
    private fun getDisplayNameColumn(type: FileType) = when (type) {
        FileType.IMAGE -> MediaStore.Images.ImageColumns.DISPLAY_NAME
        FileType.AUDIO -> MediaStore.Audio.AudioColumns.DISPLAY_NAME
        FileType.VIDEO -> MediaStore.Video.VideoColumns.DISPLAY_NAME
        else -> throw IllegalArgumentException("Unsupported kind: $type")
    }

    /**
     * Create a [MediaStore] [Uri] and returns it if successful.
     *
     * @param filename preferred filename for the media entry. System can add a suffix if there's
     * already a file with the same name. Some devices can trim the filename when special characters
     * are used.
     * @param type type of the media file to select the right collection.
     * @param location storage volume where the media file will be saved.
     * @param context [CoroutineContext] where the method will run on, default to [Dispatchers.IO].
     */
    suspend fun createMediaUri(
        filename: String,
        type: FileType,
        location: StorageLocation,
        context: CoroutineContext = Dispatchers.IO
    ): Result<Uri> = withContext(context) {
        val entry = ContentValues().apply {
            put(getDisplayNameColumn(type), filename)
        }

        val collection = when (type) {
            FileType.IMAGE -> getImageCollection(location)
            FileType.AUDIO -> getAudioCollection(location)
            FileType.VIDEO -> getVideoCollection(location)
            else -> {
                return@withContext Result.failure(IllegalArgumentException("Unsupported kind: $type"))
            }
        }

        val uri = contentResolver.insert(collection, entry)
            ?: return@withContext Result.failure(Exceptions.uriNotCreatedException(filename))

        return@withContext Result.success(uri)
    }

    /**
     * Get file path from a [MediaStore] [Uri].
     *
     * @param mediaUri [Uri] representing the MediaStore entry.
     * @param context [CoroutineContext] where the method will run on, default to [Dispatchers.IO].
     */
    private suspend fun getPathByUri(
        mediaUri: Uri,
        context: CoroutineContext = Dispatchers.IO
    ): Result<String> = withContext(context) {
        val cursor = contentResolver.query(
            mediaUri,
            arrayOf(FileColumns.DATA),
            null,
            null,
            null
        ) ?: return@withContext Result.failure(Exceptions.uriNotFoundException(mediaUri))

        cursor.use {
            if (!cursor.moveToFirst()) {
                return@withContext Result.failure(Exceptions.uriNotFoundException(mediaUri))
            }

            return@withContext Result.success(
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        FileColumns.DATA
                    )
                )
            )
        }
    }

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
     * @param context [CoroutineContext] where the method will run on, default to [Dispatchers.IO].
     */
    suspend fun addMediaFromStream(
        filename: String,
        type: FileType,
        mimeType: String,
        inputStream: InputStream,
        location: StorageLocation,
        context: CoroutineContext = Dispatchers.IO
    ): Result<Uri> = withContext(context) {
        val uri = createMediaUri(filename, type, location).getOrElse {
            return@withContext Result.failure(it)
        }

        contentResolver.openOutputStream(uri, "w").use { outputStream ->
            if (outputStream == null) {
                return@withContext Result.failure(Exceptions.unopenableOutputStreamException(uri))
            } else {
                inputStream.copyTo(outputStream)
            }
        }

        val path = getPathByUri(uri).getOrNull() ?: return@withContext Result.success(uri)
        scanFilePath(path = path, mimeType = mimeType)

        return@withContext Result.success(uri)
    }

    /**
     * Scan file path in [MediaStore] using [MediaScannerConnection]
     *
     * When adding a file (using java.io or ContentResolver APIs), MediaStore might not be aware of
     * the new entry or doesn't have an updated version of it. That's why some entries have 0 bytes
     * size, even though the file is definitely not empty. MediaStore will eventually scan the file
     * but it's better to do it ourselves to have a fresher state whenever we can.
     *
     * @param path [String] representing the file path.
     * @param mimeType mime type content.
     * @param scope [CoroutineScope] where the method will run on, default to [Dispatchers.IO].
     * TODO: Use context across all methods
     */
    private suspend fun scanFilePath(
        path: String,
        mimeType: String,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): Result<Uri> {
        return suspendCoroutine { continuation ->
            scope.launch {
                MediaScannerConnection.scanFile(
                    appContext,
                    arrayOf(path),
                    arrayOf(mimeType)
                ) { _, uri ->
                    if (uri != null) {
                        continuation.resume(Result.success(uri))
                    } else {
                        continuation.resume(Result.failure(Exceptions.fileNotScannedException(path)))
                    }
                }
            }
        }
    }

    /**
     * Scan [Uri] in [MediaStore] using [MediaScannerConnection]
     *
     * When modifying the content of a [MediaStore] entry, MediaStore might have an updated version
     * of it. That's why some entries have 0 bytes size, even though the file is definitely not
     * empty. MediaStore will eventually scan the file but it's better to do it ourselves to have a
     * fresher state whenever we can.
     *
     * @param mediaUri [Uri] representing the MediaStore entry
     * @param mimeType mime type content.
     * @param scope [CoroutineScope] where the method will run on, default to [Dispatchers.IO].
     * TODO: Use context across all methods
     */
    suspend fun scanUri(
        mediaUri: Uri,
        mimeType: String,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): Result<Boolean> {
        return suspendCoroutine { continuation ->
            scope.launch {
                val cursor = appContext.contentResolver.query(
                    mediaUri,
                    arrayOf(FileColumns.DATA),
                    null,
                    null,
                    null
                ) ?: return@launch continuation.resume(
                    Result.failure(
                        Exceptions.uriNotFoundException(
                            mediaUri
                        )
                    )
                )

                val path = cursor.use {
                    if (!cursor.moveToFirst()) {
                        return@launch continuation.resume(
                            Result.failure(
                                Exceptions.uriNotFoundException(
                                    mediaUri
                                )
                            )
                        )
                    }

                    return@use cursor.getString(cursor.getColumnIndexOrThrow(FileColumns.DATA))
                }

                scanFilePath(path, mimeType, scope)
                    .onSuccess { continuation.resume(Result.success(true)) }
                    .onFailure {
                        continuation.resume(
                            Result.failure(
                                Exceptions.uriNotScannedException(
                                    mediaUri
                                )
                            )
                        )
                    }
            }
        }
    }

    /**
     * Returns a [FileResource] if it finds its [Uri] in MediaStore otherwise returns `null`.
     *
     * @param mediaUri [Uri] representing the MediaStore entry.
     * @param context [CoroutineContext] where the method will run on, default to [Dispatchers.IO].
     */
    suspend fun getResourceByUri(
        mediaUri: Uri,
        context: CoroutineContext = Dispatchers.IO
    ): Result<FileResource> = withContext(context) {
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
        ) ?: return@withContext Result.failure(Exceptions.uriNotFoundException(mediaUri))

        cursor.use {
            if (!cursor.moveToFirst()) {
                return@withContext Result.failure(Exceptions.uriNotFoundException(mediaUri))
            }

            val displayNameColumn = cursor.getColumnIndexOrThrow(FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(FileColumns.SIZE)
//            val mediaTypeColumn = cursor.getColumnIndexOrThrow(FileColumns.MEDIA_TYPE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(FileColumns.MIME_TYPE)

            return@withContext Result.success(
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
    }
}
