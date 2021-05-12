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

import android.database.Cursor
import android.util.Log
import java.lang.Exception
import java.nio.file.DirectoryStream
import java.nio.file.Path

internal class DocumentDirectoryStream(
    private val cursor: Cursor,
    private val buildPath: (documentId: String) -> Path
) : DirectoryStream<Path> {

    private var receivedIterator = false
    private val streamIterator = object : MutableIterator<Path> {
        override fun hasNext(): Boolean {
            if (cursor.isClosed) {
                throw IllegalStateException("Directory stream is closed")
            }
            return if (cursor.isBeforeFirst) {
                cursor.moveToFirst()
            } else {
                !cursor.isLast
            }
        }

        override fun next(): Path {
            if (cursor.moveToNext()) {
                val documentId = cursor.getString(0)
                return buildPath(documentId)
            } else {
                throw NoSuchElementException("No more child documents")
            }
        }

        override fun remove() {
            throw UnsupportedOperationException("Removing is not supported")
        }
    }

    override fun close() {
        if (cursor.isClosed) {
            throw IllegalStateException("Directory stream is already closed")
        }
        cursor.close()
    }

    override fun iterator(): MutableIterator<Path> {
        if (receivedIterator) {
            throw IllegalStateException("Iterator already obtained")
        }
        if (cursor.isClosed) {
            throw IllegalStateException("Directory stream is closed")
        }
        receivedIterator = true
        return streamIterator
    }
}
