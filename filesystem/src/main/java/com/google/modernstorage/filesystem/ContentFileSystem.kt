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
import java.nio.file.FileStore
import java.nio.file.FileSystem
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.WatchService
import java.nio.file.attribute.UserPrincipalLookupService

open class ContentFileSystem internal constructor(
    private val provider: ContentFileSystemProvider,
    val authority: String
) : FileSystem() {
    private val rootUris = mutableListOf<URI>()

    override fun close() = Unit

    override fun provider() = provider

    override fun isOpen() = true

    override fun isReadOnly() = false

    override fun getSeparator() = "/"

    override fun getRootDirectories() = rootUris.map { rootUri ->
        provider.getPath(rootUri)
    }.asIterable()

    override fun getFileStores(): MutableIterable<FileStore> {
        TODO("Not yet implemented")
    }

    override fun supportedFileAttributeViews() = setOf("basic")

    open fun getPath(uri: URI) = ContentPath(this, uri)

    override fun getPath(first: String?, vararg more: String?): Path {
        val pathUri = when (first) {
            "document" -> provider.buildDocumentUri(authority, more[0]!!, false)
            "tree" -> provider.buildDocumentUri(authority, more[0]!!, true)
            else -> throw InvalidPathException(first, "Unknown path type: $first")
        }
        return getPath(pathUri)
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

    /**
     * Internal method to register a new root URI. At the moment there isn't any attempt to
     * check whether one URI is a child of a previously registered root.
     */
    internal fun registerRoot(rootUri: URI) = synchronized(rootUri) {
        if (!rootUris.contains(rootUri)) {
            rootUris.add(rootUri)
        }
    }
}
