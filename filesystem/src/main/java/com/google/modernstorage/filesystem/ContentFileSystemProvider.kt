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

import android.provider.DocumentsContract
import java.io.IOException
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.AccessMode
import java.nio.file.CopyOption
import java.nio.file.DirectoryStream
import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileStore
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider

internal const val CONTENT_SCHEME = "content"

/**
 * A [FileSystemProvider] for `content://` scheme Uris.
 *
 * Because of the way ART works, it's not possible to get an instance of this class the way you
 * would on a JVM (using [FileSystems.getFileSystem]). Instead you should explicitly create an
 * instance of this class (i.e.: `fileSystemProvider = ContentFileSystemProvider(context)`).
 */
class ContentFileSystemProvider(
    private val contentContract: PlatformContract
) : FileSystemProvider(), PlatformContract by contentContract {

    private val fileSystemCache = mutableMapOf<String, ContentFileSystem>()

    override fun getScheme() = CONTENT_SCHEME

    override fun newFileSystem(uri: URI?, env: MutableMap<String, *>?): FileSystem {
        uri ?: throw IllegalArgumentException("URI must not be null")

        val supported = contentContract.isDocumentUri(uri) || contentContract.isTreeUri(uri)
        if (!supported) {
            throw IllegalArgumentException("Only DocumentProvider URIs are currently supported")
        }

        return getOrCreateFileSystem(uri)
    }

    override fun getFileSystem(uri: URI?): FileSystem {
        val rootUri = uri ?: throw IllegalArgumentException("URI must not be null")
        return newFileSystem(rootUri, null)
    }

    override fun getPath(uri: URI?): Path {
        uri ?: throw IllegalArgumentException("URI must not be null")
        return if (uri.scheme == scheme) {
            val fileSystem = getOrCreateFileSystem(uri)

            // Perform any transformations on the incoming URI that are necessary.
            val pathUri = contentContract.prepareUri(uri)
            val treeId = contentContract.getTreeDocumentId(pathUri)
            val docId = contentContract.getDocumentId(pathUri)
                ?: throw IllegalArgumentException("URI must contain a document ID")
            fileSystem.getPath(treeId, docId)
        } else {
            throw IllegalArgumentException("URI must be a content:// uri")
        }
    }

    override fun newByteChannel(
        path: Path?,
        optsIn: MutableSet<out OpenOption>?,
        vararg attrs: FileAttribute<*>?
    ): SeekableByteChannel {
        val contentPath =
            path as? DocumentPath ?: throw IllegalArgumentException("path must be a DocumentPath")

        val options = OpenOptionFlags(optsIn)

        val pathExists = contentContract.exists(contentPath)
        if (options.create && options.exclusive && pathExists) {
            throw FileAlreadyExistsException(path.toString(), null, "$path already exists")
        } else if (options.create && !pathExists) {
            if (!contentContract.createDocument(contentPath)) {
                throw IOException("Failed to create document: $contentPath")
            }
        }

        // Based on how the default provider handles FileAttributes, there isn't anything really
        // like that available via ContentResolver (i.e.: creating a file with r/w for the owner,
        // but read only, or no access to 'group' or 'other'.)
        // The real question is whether we should silently ignore attributes or signal some sort
        // of error since we can't apply them.

        return contentContract.openByteChannel(contentPath, options.toMode())
    }

    override fun newDirectoryStream(
        path: Path?,
        filter: DirectoryStream.Filter<in Path>?
    ): DirectoryStream<Path> {
        path as? DocumentPath ?: throw IllegalArgumentException("path must be a ContentPath")
        return contentContract.newDirectoryStream(path, filter)
    }

    override fun createDirectory(dir: Path?, vararg attrs: FileAttribute<*>?) {
        dir as? DocumentPath ?: throw IllegalArgumentException("path must be a ContentPath")

        // Like `newByteChannel`, `attrs` are ignored since there isn't an obvious mapping
        // possible for a DocumentsProvider.

        if (!contentContract.exists(dir)) {
            contentContract.createDocument(dir, DocumentsContract.Document.MIME_TYPE_DIR)
        }
    }

    override fun delete(path: Path?) {
        path as? DocumentPath ?: throw IllegalArgumentException("path must be a ContentPath")
        contentContract.removeDocument(path)
    }

    override fun copy(source: Path?, target: Path?, vararg options: CopyOption?) {
        TODO("Not yet implemented")
    }

    override fun move(source: Path?, target: Path?, vararg options: CopyOption?) {
        TODO("Not yet implemented")
    }

    override fun isSameFile(path: Path, path2: Path): Boolean {
        path as? DocumentPath ?: throw IllegalArgumentException("path must be a ContentPath")
        path2 as? DocumentPath ?: throw IllegalArgumentException("path must be a ContentPath")

        // If the paths are different providers, then return false
        if (path.fileSystem.authority != path2.fileSystem.authority) return false

        // If the paths refer to different trees, then also return false
        if (path.treeId != path2.treeId) return false

        // Otherwise, since a document ID refers to a specific document, if the doc IDs are the
        // same, then it's the same file.
        return path.docId != null && path.docId == path2.docId
    }

    override fun isHidden(path: Path?): Boolean {
        path as? DocumentPath ?: throw IllegalArgumentException("path must be a ContentPath")

        // As far as I can tell, it isn't possible to have 'hidden' documents.
        return false
    }

    override fun getFileStore(path: Path?): FileStore {
        TODO("Not yet implemented")
    }

    override fun checkAccess(path: Path?, vararg modes: AccessMode?) {
        path as? DocumentPath ?: throw IllegalArgumentException("path must be a ContentPath")
        val checkModes: List<AccessMode> = modes.filterNotNull()
        contentContract.checkAccess(path, checkModes)
    }

    override fun <V : FileAttributeView?> getFileAttributeView(
        path: Path?,
        type: Class<V>?,
        vararg options: LinkOption?
    ): V {
        TODO("Not yet implemented")
    }

    override fun <A : BasicFileAttributes?> readAttributes(
        path: Path?,
        type: Class<A>?,
        vararg options: LinkOption?
    ): A {
        path as? DocumentPath ?: throw IllegalArgumentException("path must be a ContentPath")
        return contentContract.readAttributes(path, type, *options)
    }

    override fun readAttributes(
        path: Path?,
        attributes: String?,
        vararg options: LinkOption?
    ): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun setAttribute(
        path: Path?,
        attribute: String?,
        value: Any?,
        vararg options: LinkOption?
    ) {
        TODO("Not yet implemented")
    }

    private fun getOrCreateFileSystem(root: URI): ContentFileSystem {
        val authority = root.authority
        return synchronized(fileSystemCache) {
            val inCache = fileSystemCache[authority]
            if (inCache is ContentFileSystem) {
                inCache
            } else {
                return addFileSystem(ContentFileSystem(this, authority))
            }
        }
    }

    private fun addFileSystem(fileSystem: ContentFileSystem): ContentFileSystem {
        val authority = fileSystem.authority
        synchronized(fileSystemCache) {
            fileSystemCache[authority] = fileSystem
        }
        return fileSystem
    }
}
