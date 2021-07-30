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

import com.google.modernstorage.filesystem.internal.AndroidContentContract
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
     * Performs any necessary transformations on a [URI] before it can be used to create a
     * [DocumentPath]. For example, building a "document" uri when provided a "tree" uri.
     */
    fun prepareUri(incomingUri: URI): URI

    /**
     * Builds a document [URI] from the provided authority and document ID.
     * @param authority The authority of the DocumentProvider.
     * @param documentId The document ID of the document to build a URI for
     */
    fun buildDocumentUri(authority: String, documentId: String): URI

    /**
     * Builds a document URI given a base tree URI and document ID.
     */
    fun buildDocumentUriUsingTree(treeUri: URI, documentId: String): URI

    /**
     * Build URI representing access to descendant documents of the given
     * [Document#COLUMN_DOCUMENT_ID].
     *
     * @param authority The authority of the DocumentProvider.
     * @param documentId The document ID of the tree to build a URI for
     */
    fun buildTreeDocumentUri(authority: String, documentId: String): URI

    /**
     * Copies the given document.
     */
    fun copyDocument(sourceDocumentUri: URI, targetParentDocumentUri: URI)

    /**
     * Create a new document from the given path.
     *
     * @param newDocumentPath Path to create. The [Path.getFileName] is expected to be a the
     * desired value for [android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME].
     */
    fun createDocument(newDocumentPath: DocumentPath): Boolean

    /**
     * Delete the given document.
     */
    fun deleteDocument(path: DocumentPath)

    /**
     * Checks if a given path exists.
     */
    fun exists(path: DocumentPath): Boolean

    /**
     * Finds the canonical path from the top of the document tree.
     */
    fun findDocumentPath(treePath: DocumentPath): List<String>

    /**
     * Gets the document id from a DocumentsProvider backed [URI], or `null` if the
     * URI is not backed by a DocumentsProvider (or is otherwise malformed).
     */
    fun getDocumentId(documentUri: URI): String?

    /**
     * Gets the tree document ID of a given document, or `null` if the document doesn't have a
     * tree ID.
     */
    fun getTreeDocumentId(documentUri: URI): String?

    /**
     * Checks if a given URI is backed by a DocumentProvider.
     */
    fun isDocumentUri(uri: URI): Boolean

    /**
     * Checks if a provided [URI] refers to a "tree".
     */
    fun isTreeUri(uri: URI): Boolean

    /**
     * Moves the provided document from the original parent to a new parent.
     */
    fun moveDocument(
        sourceDocumentUri: URI,
        sourceParentDocumentUri: URI,
        targetParentDocumentUri: URI
    ): URI?

    /**
     * Removes a document from the parent.
     *
     * In contrast to [deleteDocument] it requires specifying the parent. This method is especially
     * useful if the document can be in multiple parents.
     */
    fun removeDocument(path: DocumentPath): Boolean

    /**
     * Changes the display name of a provided document.
     *
     * Because a DocumentProvider's document ID may depend on the display name, the URI returned
     * from this method may be different than the original.
     */
    fun renameDocument(documentUri: URI, displayName: String): URI?

    /**
     * Opens a [SeekableByteChannel] given a [URI] and mode String. The mode matches the "mode"
     * parameter of [android.content.ContentProvider.openAssetFile].
     * @see [android.content.ContentProvider.openAssetFile]
     */
    fun openByteChannel(
        path: DocumentPath,
        mode: String
    ): SeekableByteChannel

    /**
     * Builds a new [DirectoryStream] given a [DocumentPath] and [DirectoryStream.Filter].
     */
    fun newDirectoryStream(
        path: DocumentPath,
        filter: DirectoryStream.Filter<in Path>?
    ): DirectoryStream<Path>

    /**
     * Reads the attributes of a [DocumentPath]. Details of the method are described by
     * [java.nio.file.Files.readAttributes].
     * @see [java.nio.file.Files.readAttributes]
     */
    fun <A : BasicFileAttributes?> readAttributes(
        path: DocumentPath,
        type: Class<A>?,
        vararg options: LinkOption?
    ): A
}
