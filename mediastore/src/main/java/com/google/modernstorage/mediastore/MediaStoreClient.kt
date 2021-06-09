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

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

val UnaccessibleCollectionException = Exception("Collection URI could not be used")
val UriNotCreatedException = Exception("URI could not be created")
val UnopenableOutputStreamException = Exception("Output stream could not be opened")

/**
 * MediaStore client providing an easier way to interact with it.
 *
 * @constructor Creates a [MediaStoreClient] with a [Context]
 */
class MediaStoreClient(private val context: Context) {
    /**
     * Returns a [MediaResource] if it finds its content [Uri] in MediaStore otherwise returns `null`
     *
     * @param uri [Uri] representing the [MediaStore] entry.
     */
    suspend fun getResourceByUri(uri: Uri) = getMediaResourceById(context, uri)

    /**
     * Create a [MediaStore] image [Uri] and returns it if successful otherwise returns `null`
     *
     * @param filename preferred filename for the image entry. System can add a suffix if there's
     * already a file with the same name. Some devices can trim the filename when special characters
     * are used.
     * @param location storage volume where the image will be saved.
     */
    suspend fun createImageUri(filename: String, location: StorageLocation): Uri? {
        return withContext(Dispatchers.IO) {
            val entry = ContentValues().apply {
                put(MediaStore.Images.ImageColumns.DISPLAY_NAME, filename)
            }

            val collection = getImageCollection(location) ?: throw UnaccessibleCollectionException
            return@withContext context.contentResolver.insert(collection, entry)
        }
    }

    /**
     * Add an image in [MediaStore] from an [InputStream] and returns its content [Uri]
     *
     * @param filename preferred filename for the image entry. System can add a suffix if there's
     * already a file with the same name. Some devices can trim the filename when special characters
     * are used.
     * @param inputStream image content.
     * @param location storage volume where the image will be saved.
     */
    suspend fun addImageFromStream(
        filename: String,
        inputStream: InputStream,
        location: StorageLocation
    ): Uri {
        return withContext(Dispatchers.IO) {
            val imageUri = createImageUri(filename, location)

            if (imageUri == null) {
                throw UriNotCreatedException
            } else {
                context.contentResolver.openOutputStream(imageUri, "w").use { outputStream ->
                    if (outputStream == null) {
                        throw UnopenableOutputStreamException
                    } else {
                        inputStream.copyTo(outputStream)
                    }
                }

                return@withContext imageUri
            }
        }
    }

    /**
     * Create a [MediaStore] video [Uri] and returns it if successful otherwise returns `null`
     *
     * @param filename preferred filename for the video entry. System can add a suffix if there's
     * already a file with the same name. Some devices can trim the filename when special characters
     * are used.
     * @param location storage volume where the image will be saved.
     */
    suspend fun createVideoUri(filename: String, location: StorageLocation): Uri? {
        return withContext(Dispatchers.IO) {
            val entry = ContentValues().apply {
                put(MediaStore.Video.VideoColumns.DISPLAY_NAME, filename)
            }

            val collection = getVideoCollection(location) ?: throw UnaccessibleCollectionException
            return@withContext context.contentResolver.insert(collection, entry)
        }
    }

    /**
     * Add a video in [MediaStore] from an [InputStream] and returns its content [Uri]
     *
     * @param filename preferred filename for the video entry. System can add a suffix if there's
     * already a file with the same name. Some devices can trim the filename when special characters
     * are used.
     * @param inputStream video content.
     * @param location storage volume where the video will be saved.
     */
    suspend fun addVideoFromStream(
        filename: String,
        inputStream: InputStream,
        location: StorageLocation
    ): Uri {
        return withContext(Dispatchers.IO) {
            val videoUri = createVideoUri(filename, location)

            if (videoUri == null) {
                throw UriNotCreatedException
            } else {
                context.contentResolver.openOutputStream(videoUri, "w").use { outputStream ->
                    if (outputStream == null) {
                        throw UnopenableOutputStreamException
                    } else {
                        inputStream.copyTo(outputStream)
                    }
                }

                return@withContext videoUri
            }
        }
    }
}
