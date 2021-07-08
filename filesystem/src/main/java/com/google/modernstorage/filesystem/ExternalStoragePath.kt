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

import android.os.Environment
import android.os.Process
import android.provider.DocumentsContract
import java.io.File
import java.net.URI
import java.nio.file.Path

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
    fileSystem: ContentFileSystem,
    uri: URI
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

    /**
     * Validates a URI to ensure it actually matches the format of an ExternalStorageProvider
     * URI.
     *
     * Example valid URIs:
     * "content://com.android.externalstorage.documents/tree/primary%3ATest/document/primary%3ATest%2FTest.txt"
     * "content://com.android.externalstorage.documents/document/primary%3ATest%2FTest.txt"
     */
    private fun validateUri(uri: URI) {
        // "authority" has to be "com.android.externalstorage.documents"
        if (uri.authority != EXTERNAL_STORAGE_PROVIDER_AUTHORITY) {
            throw IllegalArgumentException("Bad authority: $uri")
        }
        // Both "tree" and "document" URIs have at least 3 characters
        if (uri.path.length < 2) {
            throw IllegalArgumentException("Malformed path: $uri")
        }
        // The URIs should have at least one '/' -- i.e.: document/... or tree/.../document/...
        if (uri.path.indexOf('/', 1) == -1) {
            throw IllegalArgumentException("Malformed path: $uri")
        }
        // Because the format of the docIds have a ':' in them (%3A), it has to be _before_
        // the / since the / characters divide 'tree' and 'document' parts.
        if (uri.path.indexOf('/', 1) > uri.path.indexOf(':', 1)) {
            throw IllegalArgumentException("Malformed path: $uri")
        }
        // DocumentProvider URIs never have fragments
        if (uri.fragment != null) {
            throw IllegalArgumentException("Malformed path: $uri")
        }
        // DocumentProvider URIs never have query parameters
        if (uri.query != null) {
            throw IllegalArgumentException("Malformed path: $uri")
        }
    }
}
