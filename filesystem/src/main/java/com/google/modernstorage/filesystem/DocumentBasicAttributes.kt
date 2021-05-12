/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.modernstorage.filesystem

import android.provider.DocumentsContract
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

internal fun buildDocumentBasicAttributes(
    provider: ContentFileSystemProvider,
    path: ContentPath
): DocumentBasicAttributes {
    val context = provider.applicationContext
    context.contentResolver.query(
        path.androidUri,
        arrayOf(
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE
        ),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToNext()) {
            val lastModifiedTime = FileTime.fromMillis(cursor.getLong(0))
            val mimeType = cursor.getString(1)
            val size = cursor.getLong(2)
            return DocumentBasicAttributes(
                lastModifiedTime,
                mimeType,
                size
            )
        } else {
            // No such file?
            throw FileNotFoundException("File for ${path.androidUri}")
        }
    }

    // Couldn't read the attributes
    throw IOException("Could not query ContentResolver for ${path.androidUri}")
}

public class DocumentBasicAttributes(
    private val lastModifiedTime: FileTime,
    private val mimeType: String,
    private val size: Long
) : BasicFileAttributes {

    override fun lastModifiedTime() = lastModifiedTime

    override fun lastAccessTime(): FileTime = FileTime.fromMillis(0)

    override fun creationTime(): FileTime = FileTime.fromMillis(0)

    override fun isRegularFile() = !isDirectory

    override fun isDirectory() = mimeType == DocumentsContract.Document.MIME_TYPE_DIR

    override fun isSymbolicLink() = false

    override fun isOther() = false

    override fun size() = size

    override fun fileKey() = null
}