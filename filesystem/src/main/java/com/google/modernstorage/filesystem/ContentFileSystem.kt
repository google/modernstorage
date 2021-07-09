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

import java.nio.file.FileStore
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.WatchService
import java.nio.file.attribute.UserPrincipalLookupService

open class ContentFileSystem internal constructor(
    private val provider: ContentFileSystemProvider,
    val authority: String
) : FileSystem() {
    private val roots = mutableSetOf<Path>()

    override fun close() = Unit

    override fun provider() = provider

    override fun isOpen() = true

    override fun isReadOnly() = false

    override fun getSeparator() = "/"

    override fun getRootDirectories() = roots.toMutableList().asIterable()

    override fun getFileStores(): MutableIterable<FileStore> {
        TODO("Not yet implemented")
    }

    override fun supportedFileAttributeViews() = setOf("basic")

    override fun getPath(first: String?, vararg more: String): Path {
        val path = DocumentPath(this, first, *more)
        path.root?.let { root ->
            synchronized(roots) {
                roots.add(root)
            }
        }
        return path
    }

    override fun getPathMatcher(syntaxAndPattern: String?): PathMatcher {
        TODO("Not yet implemented")
    }

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService {
        TODO("Not yet implemented")
    }

    override fun newWatchService(): WatchService {
        TODO("Not yet implemented")
    }
}
