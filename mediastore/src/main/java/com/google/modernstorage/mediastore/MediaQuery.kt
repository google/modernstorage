/*
 * Copyright 2021 The Android Open Source Project
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

import android.content.Context
import android.net.Uri
import android.provider.MediaStore.Files.FileColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getMediaResourceById(context: Context, mediaUri: Uri): MediaResource? {
    val projection = arrayOf(
        FileColumns.DISPLAY_NAME,
        FileColumns.SIZE,
//        FileColumns.MEDIA_TYPE,
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
//            val mediaTypeColumn = cursor.getColumnIndexOrThrow(FileColumns.MEDIA_TYPE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(FileColumns.MIME_TYPE)

            return@withContext MediaResource(
                uri = mediaUri,
                filename = cursor.getString(displayNameColumn),
                size = cursor.getLong(sizeColumn),
//                type = MediaType.getEnum(cursor.getInt(mediaTypeColumn)),
                type = MediaType.IMAGE,
                mimeType = cursor.getString(mimeTypeColumn),
            )
        }
    }
}
