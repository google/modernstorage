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

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.provider.DocumentsContract
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.AccessMode
import java.nio.file.CopyOption
import java.nio.file.DirectoryStream
import java.nio.file.FileStore
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.WRITE
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider

/**
 * The authority for `ExternalStorageProvider`. This is a constant in [DocumentsContract],
 * but it's marked as `@hide`, so the value is replicated here.
 */
internal const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"

/**
 * A [FileSystemProvider] for `content://` scheme Uris.
 *
 * Because of the way ART works, it's not possible to get an instance of this class the way you
 * would on a JVM (using [FileSystems.getFileSystem]). Instead you should explicitly create an
 * instance of this class (i.e.: `fileSystemProvider = ContentFileSystemProvider(context)`).
 */
class ContentFileSystemProvider(internal val applicationContext: Context) : FileSystemProvider() {

    companion object {
        private val fileSystemCache = mutableMapOf<String, ContentFileSystem>()
    }

    override fun getScheme() = ContentResolver.SCHEME_CONTENT

    override fun newFileSystem(uri: URI?, env: MutableMap<String, *>?): FileSystem {
        val rootUri = uri ?: throw IllegalArgumentException("URI must not be null")
        val androidUri = rootUri.toUri()

        if (!DocumentsContract.isDocumentUri(applicationContext, androidUri)) {
            throw IllegalArgumentException("Only DocumentProvider URIs are currently supported")
        }

        return getOrCreateFileSystem(uri.authority)
    }

    override fun getFileSystem(uri: URI?): FileSystem {
        val rootUri = uri ?: throw IllegalArgumentException("URI must not be null")
        return newFileSystem(rootUri, null)
    }

    override fun getPath(uri: URI?): Path {
        val checkUri = uri ?: throw IllegalArgumentException("URI must not be null")
        return if (checkUri.scheme == scheme) {
            val authority = checkUri.authority
            val fileSystem = getOrCreateFileSystem(authority)

            /*
             * Uris returned from `ACTION_OPEN_DOCUMENT_TREE` cannot be used directly (via
             * `ContentResolver#query()` for example), but have to be converted to a
             * "document Uri". If the caller does that, it's fine. If the caller doesn't do that,
             * then this code will do the conversion automatically.
             */
            val androidUri = uri.toUri()
            val isTreeUri = DocumentsContract.isTreeUri(androidUri)
            val isDocumentUri = DocumentsContract.isDocumentUri(applicationContext, androidUri)
            val pathUri = if (isTreeUri && !isDocumentUri) {
                val documentId = DocumentsContract.getTreeDocumentId(androidUri)
                DocumentsContract.buildDocumentUriUsingTree(androidUri, documentId).toURI()
            } else {
                androidUri.toURI()
            }

            when (authority) {
                EXTERNAL_STORAGE_PROVIDER_AUTHORITY -> ExternalStoragePath(fileSystem, pathUri)
                else -> ContentPath(fileSystem, pathUri)
            }
        } else {
            throw IllegalArgumentException("URI must be a content:// uri")
        }
    }

    override fun newByteChannel(
        path: Path?,
        options: MutableSet<out OpenOption>?,
        vararg attrs: FileAttribute<*>?
    ): SeekableByteChannel {
        val contentPath =
            path as? ContentPath ?: throw IllegalArgumentException("path must be a ContentPath")
        val modeString = if (options.isNullOrEmpty()) {
            // By default, open for reading only
            "r"
        } else {
            options.optionToMode(READ, "r") + options.optionToMode(WRITE, "w") +
                    options.optionToMode(APPEND, "a") + options.optionToMode(TRUNCATE_EXISTING, "t")
        }

        // Fix for https://issuetracker.google.com/180526528
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && modeString == "w") {
            "rwt"
        } else {
            modeString
        }

        // TODO: Support providing attributes.

        val uri = contentPath.androidUri
        return applicationContext.contentResolver.openFileDescriptor(uri, mode)?.let { fd ->
            FileInputStream(fd.fileDescriptor).channel
        } ?: throw FileNotFoundException("openFileDescriptor($uri) returned null")
    }

    override fun newDirectoryStream(
        path: Path?,
        filter: DirectoryStream.Filter<in Path>?
    ): DirectoryStream<Path> {
        val rootPath =
            path as? ContentPath ?: throw IllegalArgumentException("path must be a ContentPath")
        val rootUri = rootPath.androidUri

        val childDocsUri = path.childDocumentsUri
        val contentResolver = applicationContext.contentResolver
        val cursor = contentResolver.query(
            childDocsUri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
            null,
            null,
            null,
            null
        )
        return cursor?.let {
            DocumentDirectoryStream(cursor) { documentId ->
                val childUri = if (path.isTree) {
                    DocumentsContract.buildDocumentUriUsingTree(rootUri, documentId)
                } else {
                    DocumentsContract.buildChildDocumentsUri(rootUri.authority, documentId)
                }
                getPath(childUri.toURI())
            }
        } ?: throw FileNotFoundException("No files found for path $rootUri")
    }

    override fun createDirectory(dir: Path?, vararg attrs: FileAttribute<*>?) {
        TODO("Not yet implemented")
    }

    override fun delete(path: Path?) {
        TODO("Not yet implemented")
    }

    override fun copy(source: Path?, target: Path?, vararg options: CopyOption?) {
        TODO("Not yet implemented")
    }

    override fun move(source: Path?, target: Path?, vararg options: CopyOption?) {
        TODO("Not yet implemented")
    }

    override fun isSameFile(path: Path?, path2: Path?): Boolean {
        TODO("Not yet implemented")
    }

    override fun isHidden(path: Path?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getFileStore(path: Path?): FileStore {
        TODO("Not yet implemented")
    }

    override fun checkAccess(path: Path?, vararg modes: AccessMode?) {
        TODO("Not yet implemented")
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
        val contentPath =
            path as? ContentPath ?: throw IllegalArgumentException("path must be a ContentPath")
        try {
            val attributes = when (type) {
                BasicFileAttributes::class.java -> buildDocumentBasicAttributes(this, contentPath)
                DocumentBasicAttributes::class.java -> buildDocumentBasicAttributes(
                    this,
                    contentPath
                )
                else -> {
                    throw IllegalArgumentException("Reading $type attributes is not supported")
                }
            }

            // Tell Kotlin it's all fine
            return type.cast(attributes)
                ?: throw IllegalArgumentException("Reading $type attributes is not supported")
        } catch (ex: Exception) {
            throw IOException("Failed to read attributes for ${path.androidUri}", ex)
        }
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

    private fun Set<OpenOption>.optionToMode(openOption: OpenOption, modeString: String) =
        if (contains(openOption)) modeString else ""

    private fun getOrCreateFileSystem(authority: String): ContentFileSystem {
        synchronized(fileSystemCache) {
            val inCache = fileSystemCache[authority]
            if (inCache is ContentFileSystem) {
                return inCache
            }

            val newFileSystem = ContentFileSystem(this)
            fileSystemCache[authority] = newFileSystem
            return newFileSystem
        }
    }
}