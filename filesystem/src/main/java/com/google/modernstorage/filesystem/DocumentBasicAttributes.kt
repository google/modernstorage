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

import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

class DocumentBasicAttributes internal constructor(
    private val lastModifiedTime: FileTime,
    val mimeType: String,
    private val size: Long,
    private val isFolder: Boolean
) : BasicFileAttributes {

    override fun lastModifiedTime() = lastModifiedTime

    override fun lastAccessTime(): FileTime = FileTime.fromMillis(0)

    override fun creationTime(): FileTime = FileTime.fromMillis(0)

    override fun isRegularFile() = !isDirectory

    override fun isDirectory() = isFolder

    override fun isSymbolicLink() = false

    override fun isOther() = false

    override fun size() = size

    override fun fileKey() = null

    override fun toString() =
        "DocumentBasicAttributes{mimeType:$mimeType, size:$size, isFolder:$isFolder, " +
            "lastModifiedTime=$lastModifiedTime}"
}
