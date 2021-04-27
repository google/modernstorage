package com.google.modernstorage.media

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

val UnaccessibleCollectionException = Exception("Collection URI could not be used")
val UriNotCreatedException = Exception("URI could not be created")
val UnopenableOutputStreamException = Exception("Output stream could not be opened")

// TODO: Handle Bitmap input
// TODO: Handle ByteArray input

class MediaStoreClient(private val context: Context) {
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
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            addImageFromStreamApi21(filename, inputStream, location)
        } else {
            addImageFromStreamApi21(filename, inputStream, location)
        }
    }

    private suspend fun addImageFromStreamApi21(
        filename: String,
        inputStream: InputStream,
        location: StorageLocation
    ) {
        withContext(Dispatchers.IO) {
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
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            addVideoFromStreamApi21(filename, inputStream, location)
        } else {
            addVideoFromStreamApi21(filename, inputStream, location)
        }
    }

    private suspend fun addVideoFromStreamApi21(
        filename: String,
        inputStream: InputStream,
        location: StorageLocation
    ) {
        withContext(Dispatchers.IO) {
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
            }
        }
    }
}