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
package com.google.modernstorage.filesystem

import java.net.URI
import java.nio.file.Path

/**
 * Implementation of a [java.nio.file.FileSystem] specific for Android's ExternalStorageProvider.
 */
@Deprecated("Use the new storage module instead, this module will be removed at the next version")
class ExternalStorageFileSystem internal constructor(
    provider: ContentFileSystemProvider,
) : ContentFileSystem(provider, EXTERNAL_STORAGE_PROVIDER_AUTHORITY) {

    override fun getPath(uri: URI) = ExternalStoragePath(this, uri)

    override fun getPath(first: String?, vararg more: String?): Path {
        if (more.isNullOrEmpty()) {
            throw NullPointerException("Cannot build a path with a null document ID")
        }

        /*
         * This assumes the docId format is root:path/to/file
         * (Based on the comment in AOSP's ExternalStorageProvider.java)
         *
         * Because of this, the first element of "more" is "root", and the remaining
         * elements would be the directories (path, to, file).
         * %3A and %2F are just URL escaped versions of ":" and "/", respectively.
         *
         * Example:
         * input: first="tree", more=["primary", "Documents", "MyApp", "Doc.txt"]
         * output: "primary%3ADocuments%2FMyApp%2FDoc.txt"
         */
        val documentId = more.fold("") { acc, part ->
            if (acc.isEmpty()) {
                "$part%3A"
            } else {
                "$acc$part%2F"
            }
        }
        return super.getPath(first, documentId)
    }
}
