/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.modernstorage.filesystem

import android.os.Environment
import android.os.Process
import android.provider.DocumentsContract
import java.io.File
import java.net.URI
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

/**
 * The authority for `ExternalStorageProvider`. This is a constant in [DocumentsContract],
 * but it's marked as `@hide`, so the value is replicated here.
 */
internal const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"

/**
 * Representation of a `content://` URI backed by Android's
 * `com.android.externalstorage.ExternalStorageProvider`.
 */
class ExternalStoragePath internal constructor(
    fileSystem: ContentFileSystem, uri: URI
) : ContentPath(fileSystem, uri) {

    private val mountRoot: String

    init {
        validateUri(uri)

        mountRoot = uri.path.substringBefore(":").substringAfterLast('/')
    }

    override fun compareTo(other: Path?): Int {
        // If both are ExternalStoragePaths, then the scheme and authority must be the same,
        // so the only difference could be the URI paths.
        other as? ExternalStoragePath
            ?: throw ClassCastException("Cannot compare to a non-ExternalStoragePath Path")
        val otherPath = other.toUri().path
        return otherPath.compareTo(toUri().path, ignoreCase = true)
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

    override fun normalize(): ContentPath {
        TODO("Not yet implemented")
    }

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

    override fun relativize(other: Path?): Path {
        TODO("Not yet implemented")
    }

    override fun toUri(): URI = uri

    override fun toAbsolutePath(): Path {
        TODO("Not yet implemented")
    }

    override fun toRealPath(vararg options: LinkOption?): Path {
        TODO("Not yet implemented")
    }

    /**
     * Returns a `File` object representing this path.
     *
     * If the URI backing this Path cannot be parsed to a `File`, an [UnsupportedOperationException]
     * will be thrown. If the app does not have access to check if the `File` exists,
     * a [SecurityException] will be thrown.
     */
    @Throws(UnsupportedOperationException::class, SecurityException::class)
    override fun toFile(): File {
        val rootFile = if (mountRoot == "primary") {
            @Suppress("deprecation")
            Environment.getExternalStorageDirectory()
        } else {
            File("/storage/$mountRoot")
        }
        val documentPath = uri.path?.substringAfterLast(":")
            ?: throw UnsupportedOperationException("Cannot convert $uri to a File")
        val asFile = File(rootFile, documentPath)
        if (asFile.exists()) {
            return asFile
        } else {
            throw SecurityException("${Process.myPid()} does not have access to $asFile")
        }
    }

    private fun validateUri(uri: URI) {
        if (uri.authority != EXTERNAL_STORAGE_PROVIDER_AUTHORITY) {
            throw IllegalArgumentException("Bad authority: $uri")
        }
        if (uri.path.length < 2) {
            throw IllegalArgumentException("Malformed path: $uri")
        }
        if (uri.path.indexOf('/', 1) == -1) {
            throw IllegalArgumentException("Malformed path: $uri")
        }
        if (uri.path.indexOf('/', 1) > uri.path.indexOf(':', 1)) {
            throw IllegalArgumentException("Malformed path: $uri")
        }
        if (uri.fragment != null) {
            throw IllegalArgumentException("Malformed path: $uri")
        }
        if (uri.query != null) {
            throw IllegalArgumentException("Malformed path: $uri")
        }
    }
}

