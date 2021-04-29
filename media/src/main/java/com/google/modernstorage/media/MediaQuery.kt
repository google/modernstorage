package com.google.modernstorage.media

import android.content.Context
import android.net.Uri
import android.provider.MediaStore.Files.FileColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getMediaResourceById(context: Context, mediaUri: Uri): MediaResource? {
    val projection = arrayOf(
        FileColumns.DISPLAY_NAME,
        FileColumns.SIZE,
        FileColumns.MEDIA_TYPE,
        FileColumns.MIME_TYPE,
    )

    return withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(
            mediaUri,
            projection,
            null,
            null,
            null
        ) ?: return@withContext null

        cursor.use {
            if (!cursor.moveToFirst()) {
                return@withContext null
            }

            val displayNameColumn = cursor.getColumnIndexOrThrow(FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(FileColumns.SIZE)
            val mediaTypeColumn = cursor.getColumnIndexOrThrow(FileColumns.MEDIA_TYPE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(FileColumns.MIME_TYPE)

            return@withContext MediaResource(
                uri = mediaUri,
                filename = cursor.getString(displayNameColumn),
                size = cursor.getLong(sizeColumn),
                type = MediaType.getEnum(cursor.getInt(mediaTypeColumn)),
                mimeType = cursor.getString(mimeTypeColumn),
            )
        }
    }
}