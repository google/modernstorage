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

import android.net.Uri
import android.provider.DocumentsContract
import java.io.File
import java.net.URI
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

/**
 * Base class representing a generic `content://` scheme [Uri] as a [Path]
 */
open class ContentPath(private val fs: ContentFileSystem, protected val uri: URI) : Path {
    internal val androidUri = Uri.parse(uri.toString())
    internal open val isTree = DocumentsContract.isTreeUri(androidUri)

    /**
     * Uri that can be queried to get a list of child documents (if any).
     */
    internal open val childDocumentsUri
        get() = DocumentsContract.buildChildDocumentsUri(
            androidUri.authority,
            DocumentsContract.getDocumentId(androidUri)
        )

    override fun compareTo(other: Path?): Int {
        // Use `!!` to assert non-null (or throw a more useful exception)
        return (other!! as? ContentPath)?.androidUri?.compareTo(androidUri)
            ?: throw ClassCastException("Cannot compare to a non-ContentPath Path")
    }

    override fun iterator(): MutableIterator<Path> {
        TODO("Not yet implemented")
    }

    override fun register(
        watcher: WatchService?,
        events: Array<out WatchEvent.Kind<*>>?,
        vararg modifiers: WatchEvent.Modifier?
    ): WatchKey {
        TODO("Not yet implemented")
    }

    override fun register(watcher: WatchService?, vararg events: WatchEvent.Kind<*>?): WatchKey {
        TODO("Not yet implemented")
    }

    override fun getFileSystem() = fs

    override fun isAbsolute(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getRoot(): Path {
        TODO("Not yet implemented")
    }

    override fun getFileName(): Path {
        TODO("Not yet implemented")
    }

    override fun getParent(): Path {
        TODO("Not yet implemented")
    }

    override fun getNameCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getName(index: Int): Path {
        TODO("Not yet implemented")
    }

    override fun subpath(beginIndex: Int, endIndex: Int): Path {
        TODO("Not yet implemented")
    }

    override fun startsWith(other: Path?): Boolean {
        TODO("Not yet implemented")
    }

    override fun startsWith(other: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun endsWith(other: Path?): Boolean {
        TODO("Not yet implemented")
    }

    override fun endsWith(other: String?): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns a path that is this path with redundant name elements eliminated.
     *
     * Because ContentPath paths are generic, there isn't a generic way to normalize them. As such
     * this method always returns the same path.
     */
    override fun normalize() = this

    override fun resolve(other: Path?): Path {
        TODO("Not yet implemented")
    }

    override fun resolve(other: String?): Path {
        TODO("Not yet implemented")
    }

    override fun resolveSibling(other: Path?): Path {
        TODO("Not yet implemented")
    }

    override fun resolveSibling(other: String?): Path {
        TODO("Not yet implemented")
    }

    /**
     * Constructs a relative path between this path and a given path.
     *
     * Because ContentPaths are generic, constructing relative paths is unsupported.
     */
    override fun relativize(other: Path?): Path {
        throw IllegalArgumentException("ContentPath cannot be made relative")
    }

    override fun toUri() = uri

    override fun toAbsolutePath(): Path {
        TODO("Not yet implemented")
    }

    override fun toRealPath(vararg options: LinkOption?): Path {
        return this
    }

    override fun toFile(): File {
        throw UnsupportedOperationException()
    }
}