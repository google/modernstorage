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
import java.nio.channels.SeekableByteChannel
import java.nio.file.DirectoryStream
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

/**
 * Contract interface for platform specific implementations of methods required by
 * [ContentFileSystemProvider].
 *
 * This interface is nominally implemented by [AndroidContentContract].
 */
interface PlatformContract {

    /**
     * Checks if a [URI] is supported by the [ContentFileSystemProvider].
     */
    fun isSupportedUri(uri: URI): Boolean

    /**
     * Checks if a provided [URI] refers to a "tree".
     */
    fun isTreeUri(uri: URI): Boolean

    /**
     * Performs any necessary transformations on a [URI] before it can be used to create a
     * [ContentPath]. For example, building a "document" uri when provided a "tree" uri.
     */
    fun prepareUri(incomingUri: URI): URI

    /**
     * Gets the document id from a DocumentsProvider backed [URI], or `null` if the
     * URI is not backed by a DocumentsProvider (or is otherwise malformed).
     */
    fun getDocumentId(documentUri: URI): String?

    /**
     * Builds a document [URI] from the provided authority and document ID.
     * @param authority The authority of the DocumentProvider.
     * @param documentId The document ID of the document to build a URI for
     * @param buildTree `true` to build a URI for a "tree", `false` to build it as a "document" URI.
     */
    fun buildDocumentUri(authority: String, documentId: String, buildTree: Boolean): URI

    /**
     * Opens a [SeekableByteChannel] given a [URI] and mode String. The mode matches the "mode"
     * parameter of [android.content.ContentProvider.openAssetFile].
     * @see [android.content.ContentProvider.openAssetFile]
     */
    fun openByteChannel(uri: URI, mode: String): SeekableByteChannel

    /**
     * Builds a new [DirectoryStream] given a [ContentPath] and [DirectoryStream.Filter].
     */
    fun newDirectoryStream(
        path: ContentPath,
        filter: DirectoryStream.Filter<in Path>?
    ): DirectoryStream<Path>

    /**
     * Reads the attributes of a [ContentPath]. Details of the method are described by
     * [java.nio.file.Files.readAttributes].
     * @see [java.nio.file.Files.readAttributes]
     */
    fun <A : BasicFileAttributes?> readAttributes(
        path: ContentPath,
        type: Class<A>?,
        vararg options: LinkOption?
    ): A
}
