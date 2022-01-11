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

import java.io.File
import java.net.URI
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.util.Objects

/**
 * Base class representing a generic `content://` scheme as a [Path]
 */
@Deprecated("Use the new storage module instead, this module will be removed at the next version")
open class ContentPath(private val fs: ContentFileSystem, protected val uri: URI) : Path {

    override fun compareTo(other: Path?): Int {
        // Use `!!` to assert non-null (or throw a more useful exception)
        return (other!! as? ContentPath)?.toUri()?.compareTo(uri)
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

    // Because a content provider doesn't have a well defined uri structure, and because this
    // method is not allowed to work with the filesystem itself, there's no way to implement
    // this method generally.
    override fun getParent(): Path? {
        val elements = elements()
        return if (elements.size > 1) {
            subpath(0, elements.size - 1)
        } else {
            null
        }
    }

    override fun getNameCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getName(index: Int): Path {
        TODO("Not yet implemented")
    }

    override fun subpath(beginIndex: Int, endIndex: Int): Path {
        val elements = elements()

        require(beginIndex >= 0)
        require(beginIndex < elements.size)
        require(beginIndex < endIndex)
        require(endIndex <= elements.size)

        val treeOrDocument = if (fileSystem.provider().isTreeUri(uri)) "tree" else "document"
        return fileSystem.getPath(
            treeOrDocument,
            *elements.subList(beginIndex, endIndex).toTypedArray()
        )
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

    /**
     * Returns a list of the elements that make up this path.
     * By default, this is a list containing only a single element: the entire 'path' of the
     * path's backing [URI].
     * The elements returned by this must be able to be passed to [ContentFileSystem.getPath] to
     * build this same path.
     */
    protected open fun elements(): List<String> = listOf(fileSystem.provider().getDocumentId(uri)!!)

    override fun equals(other: Any?): Boolean {
        other as? ContentPath
            ?: throw ClassCastException("Cannot compare to a non-ContentPath Path")
        return compareTo(other) == 0
    }

    override fun hashCode() = Objects.hash(uri)

    override fun toString() =
        "${javaClass.simpleName}@${System.identityHashCode(this).toString(16)}($uri)"
}
