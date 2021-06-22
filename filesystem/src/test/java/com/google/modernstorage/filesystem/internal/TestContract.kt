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

import com.google.modernstorage.filesystem.CONTENT_SCHEME
import com.google.modernstorage.filesystem.ContentPath
import com.google.modernstorage.filesystem.PlatformContract
import java.lang.ClassCastException
import java.lang.IllegalArgumentException
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.DirectoryStream
import java.nio.file.DirectoryStream.Filter
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

/**
 * Implementation of a [PlatformContract] for host side tests.
 */
class TestContract(
    val getDocumentIdImpl: (URI) -> String? = { _ -> TODO() },
    val openByteChannelImpl: (URI, String) -> SeekableByteChannel = { _, _ -> TODO() },
    val newDirectoryStreamImpl: (ContentPath, Filter<in Path>?) -> DirectoryStream<Path> = { _, _ -> TODO() },
    val readAttributesImpl: (ContentPath, options: Array<out LinkOption?>) -> BasicFileAttributes = { _, _ -> TODO() },
) : PlatformContract {

    override fun isSupportedUri(uri: URI) = uri.scheme == CONTENT_SCHEME

    override fun isTreeUri(uri: URI): Boolean = uri.path.contains("/tree/")

    override fun prepareUri(incomingUri: URI) = incomingUri

    override fun getDocumentId(documentUri: URI) = getDocumentIdImpl(documentUri)

    override fun buildDocumentUri(authority: String, documentId: String, buildTree: Boolean): URI {
        val uriString = if (buildTree) {
            val rootId = documentId.substringBefore("%3A")
            "content://$authority/tree/$rootId/document/$documentId"
        } else {
            "content://$authority/document/$documentId"
        }
        return URI(uriString)
    }

    override fun openByteChannel(uri: URI, mode: String) = openByteChannelImpl(uri, mode)

    override fun newDirectoryStream(
        path: ContentPath,
        filter: Filter<in Path>?
    ) = newDirectoryStreamImpl(path, filter)

    override fun <A : BasicFileAttributes?> readAttributes(
        path: ContentPath,
        type: Class<A>?,
        vararg options: LinkOption?
    ): A {
        return type!!.cast(readAttributesImpl(path, options))!!
    }
}