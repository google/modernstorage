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
package com.google.modernstorage.filesystem.internal

import com.google.modernstorage.filesystem.DocumentPath
import com.google.modernstorage.filesystem.PlatformContract
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.AccessMode
import java.nio.file.DirectoryStream
import java.nio.file.DirectoryStream.Filter
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

/**
 * Implementation of a [PlatformContract] for host side tests.
 */
class TestContract(
    var checkAccessImpl: (DocumentPath, List<AccessMode>) -> Unit = { _, _ -> TODO() },
    var createDocumentImpl: (DocumentPath, String?) -> Boolean = { _, _ -> TODO() },
    var existsImpl: (DocumentPath) -> Boolean = { _ -> TODO() },
    var openByteChannelImpl: (DocumentPath, String) -> SeekableByteChannel = { _, _ -> TODO() },
    var newDirectoryStreamImpl: (DocumentPath, Filter<in Path>?) -> DirectoryStream<Path> = { _, _ -> TODO() },
    var readAttributesImpl: (DocumentPath, options: Array<out LinkOption?>) -> BasicFileAttributes = { _, _ -> TODO() },
    var findDocumentPathImpl: (DocumentPath) -> List<String> = { _ -> TODO() },
) : PlatformContract {

    override fun prepareUri(incomingUri: URI) = incomingUri

    override fun buildDocumentUri(authority: String, documentId: String): URI {
        return URI("content://$authority/document/$documentId")
    }

    override fun buildDocumentUriUsingTree(treeUri: URI, documentId: String): URI {
        val authority = treeUri.authority
        val treeDocumentId = getTreeDocumentId(treeUri)
        return URI("content://$authority/tree/$treeDocumentId/document/$documentId")
    }

    override fun buildTreeDocumentUri(authority: String, documentId: String): URI {
        return URI("content://$authority/tree/$documentId")
    }

    override fun checkAccess(path: DocumentPath, modes: List<AccessMode>) =
        checkAccessImpl(path, modes)

    override fun copyDocument(sourceDocumentUri: URI, targetParentDocumentUri: URI) {
        TODO("Not yet implemented")
    }

    override fun createDocument(newDocumentPath: DocumentPath, mimeType: String?) =
        createDocumentImpl(newDocumentPath, mimeType)

    override fun deleteDocument(path: DocumentPath) {
        TODO("Not yet implemented")
    }

    override fun exists(path: DocumentPath) = existsImpl(path)

    override fun findDocumentPath(treePath: DocumentPath) = findDocumentPathImpl(treePath)

    override fun getDocumentId(documentUri: URI): String {
        val path = documentUri.path.substring(1).split('/', limit = 4)
        return if (path.size > 2 && path[0] == "tree") {
            path[3]
        } else if (path.size == 2) {
            path[1]
        } else {
            throw IllegalArgumentException("Invalid URI")
        }
    }

    override fun getTreeDocumentId(documentUri: URI): String {
        val path = documentUri.path.substring(1).split('/')
        return if (path.size >= 2 && path[0] == "tree") {
            path[1]
        } else {
            throw IllegalArgumentException("Invalid URI")
        }
    }

    override fun isDocumentUri(uri: URI): Boolean {
        val path = uri.path.substring(1).split('/')
        return path.size >= 2 && (path[0] == "tree" || path[0] == "document")
    }

    override fun isTreeUri(uri: URI): Boolean = uri.path.contains("/tree/")

    override fun moveDocument(
        sourceDocumentUri: URI,
        sourceParentDocumentUri: URI,
        targetParentDocumentUri: URI
    ): URI? {
        TODO("Not yet implemented")
    }

    override fun renameDocument(documentUri: URI, displayName: String): URI? {
        TODO("Not yet implemented")
    }

    override fun removeDocument(path: DocumentPath): Boolean {
        TODO("Not yet implemented")
    }

    override fun openByteChannel(path: DocumentPath, mode: String) =
        openByteChannelImpl(path, mode)

    override fun newDirectoryStream(
        path: DocumentPath,
        filter: Filter<in Path>?
    ) = newDirectoryStreamImpl(path, filter)

    override fun <A : BasicFileAttributes?> readAttributes(
        path: DocumentPath,
        type: Class<A>?,
        vararg options: LinkOption?
    ): A {
        return type!!.cast(readAttributesImpl(path, options))!!
    }
}
