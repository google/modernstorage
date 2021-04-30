package com.google.modernstorage.media

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

class MediaStoreClient(private val context: Context) {
    suspend fun getResourceByUri(uri: Uri) = getMediaResourceById(context, uri)

    suspend fun createImageUri(filename: String, location: StorageLocation): Uri? {
        return withContext(Dispatchers.IO) {
            val entry = ContentValues().apply {
                put(MediaStore.Images.ImageColumns.DISPLAY_NAME, filename)
            }

            val collection = getImageCollection(location) ?: throw UnaccessibleCollectionException
            return@withContext context.contentResolver.insert(collection, entry)
        }
    }

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

    suspend fun createVideoUri(filename: String, location: StorageLocation): Uri? {
        return withContext(Dispatchers.IO) {
            val entry = ContentValues().apply {
                put(MediaStore.Video.VideoColumns.DISPLAY_NAME, filename)
            }

            val collection = getVideoCollection(location) ?: throw UnaccessibleCollectionException
            return@withContext context.contentResolver.insert(collection, entry)
        }
    }

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