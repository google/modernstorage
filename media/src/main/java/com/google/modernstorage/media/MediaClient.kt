package com.google.modernstorage.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore

val UriNotCreatedException = Exception("URI could not be created")

class MediaClient(private val context: Context) {
    private suspend fun createUri(
        filename: String,
        collection: Uri,
        displayNameColumn: String
    ): Uri? {
        val entry = ContentValues().apply {
            put(displayNameColumn, filename)
        }

        return context.contentResolver.insert(collection, entry)
    }

    private fun getImageCollection(location: StorageLocation): Uri {
        return when (location) {
            Internal -> MediaStore.Images.Media.INTERNAL_CONTENT_URI
            SharedPrimary -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    }

    suspend fun addImageAPI21(
        filename: String,
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat,
        quality: Int = 100,
        location: StorageLocation
    ) {
        val imageUri = createUri(
            filename,
            getImageCollection(location),
            MediaStore.Images.ImageColumns.DISPLAY_NAME
        )

        if (imageUri == null) {
            throw UriNotCreatedException
        } else {
            context.contentResolver.openOutputStream(imageUri, "w").use { stream ->
                bitmap.compress(compressFormat, quality, stream)
            }
        }
    }

    suspend fun addImageAPI29(
        filename: String,
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat,
        quality: Int = 100,
        location: StorageLocation
    ) {
        val entry = ContentValues().apply {
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, filename)
        }

//        return context.contentResolver.insert(collection, entry)
        val imageUri = createUri(
            filename,
            getImageCollection(location),
            MediaStore.Images.ImageColumns.DISPLAY_NAME
        )

        if (imageUri == null) {
            throw UriNotCreatedException
        } else {
            context.contentResolver.openOutputStream(imageUri, "w").use { stream ->
                bitmap.compress(compressFormat, quality, stream)
            }
        }

    }
}