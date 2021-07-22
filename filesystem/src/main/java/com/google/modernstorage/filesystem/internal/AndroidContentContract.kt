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

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME
import android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID
import android.webkit.MimeTypeMap
import com.example.myapplication.FileDescriptorChannel
import com.google.modernstorage.filesystem.DocumentBasicAttributes
import com.google.modernstorage.filesystem.DocumentPath
import com.google.modernstorage.filesystem.PlatformContract
import com.google.modernstorage.filesystem.SequenceDocumentDirectoryStream
import com.google.modernstorage.filesystem.toURI
import com.google.modernstorage.filesystem.toUri
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.DirectoryStream
import java.nio.file.DirectoryStream.Filter
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

class AndroidContentContract(context: Context) : PlatformContract {
    private val context = context.applicationContext

    override fun prepareUri(incomingUri: URI): URI {
        /*
         * Uris returned from `ACTION_OPEN_DOCUMENT_TREE` cannot be used directly (via
         * `ContentResolver#query()` for example), but have to be converted to a
         * "document Uri". If the caller does that, it's fine. If the caller doesn't do that,
         * then this code will do the conversion automatically.
         */
        val androidUri = incomingUri.toUri()
        val isTreeUri = DocumentsContract.isTreeUri(androidUri)
        val isDocumentUri = DocumentsContract.isDocumentUri(context, androidUri)
        return if (isTreeUri && !isDocumentUri) {
            val documentId = DocumentsContract.getTreeDocumentId(androidUri)
            DocumentsContract.buildDocumentUriUsingTree(androidUri, documentId).toURI()
        } else {
            incomingUri
        }
    }

    override fun buildDocumentUri(authority: String, documentId: String) =
        DocumentsContract.buildDocumentUri(authority, documentId).toURI()

    override fun buildDocumentUriUsingTree(treeUri: URI, documentId: String) =
        DocumentsContract.buildDocumentUriUsingTree(treeUri.toUri(), documentId).toURI()

    override fun buildTreeDocumentUri(authority: String, documentId: String) =
        DocumentsContract.buildTreeDocumentUri(authority, documentId).toURI()

    override fun createDocument(parentDocumentUri: URI, mimeType: String, displayName: String) {
        TODO("Not yet implemented")
    }

    override fun copyDocument(sourceDocumentUri: URI, targetParentDocumentUri: URI) {
        TODO("Not yet implemented")
    }

    override fun deleteDocument(documentUri: URI) {
        TODO("Not yet implemented")
    }

    override fun findDocumentPath(treePath: DocumentPath): List<String> = try {
        DocumentsContract.findDocumentPath(
            context.contentResolver, treePath.androidUri
        )?.path ?: emptyList()
    } catch (_: UnsupportedOperationException) {
        emptyList()
    }

    override fun getDocumentId(documentUri: URI): String? {
        return try {
            DocumentsContract.getDocumentId(documentUri.toUri())
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    override fun getTreeDocumentId(documentUri: URI): String? {
        return try {
            DocumentsContract.getTreeDocumentId(documentUri.toUri())
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    override fun isDocumentUri(uri: URI) = DocumentsContract.isDocumentUri(context, uri.toUri())

    override fun isTreeUri(uri: URI) = DocumentsContract.isTreeUri(uri.toUri())

    override fun moveDocument(
        sourceDocumentUri: URI,
        sourceParentDocumentUri: URI,
        targetParentDocumentUri: URI
    ): URI? {
        TODO("Not yet implemented")
    }

    override fun removeDocument(documentUri: URI, parentDocumentUri: URI): Boolean {
        TODO("Not yet implemented")
    }

    override fun renameDocument(documentUri: URI, displayName: String): URI? {
        TODO("Not yet implemented")
    }

    override fun openByteChannel(
        path: DocumentPath,
        options: MutableSet<out OpenOption>
    ): SeekableByteChannel {
        val mode = options.map { option ->
            when (option) {
                StandardOpenOption.APPEND -> "a"
                StandardOpenOption.READ -> "r"
                StandardOpenOption.TRUNCATE_EXISTING -> "t"
                StandardOpenOption.WRITE -> "w"
                else -> ""
            }
        }.sorted().joinToString("")

        // Fix for https://issuetracker.google.com/180526528
        val openMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mode == "w") {
            "rwt"
        } else if (mode == "tw") {
            // The OpenJDK will pass the options WRITE + TRUNCATE_EXISTING together, but Android's
            // ParcelFileDescriptor doesn't handle "tw" as a valid mode, so we need to change it
            // to also include reading, which is then supported again.
            "rwt"
        } else {
            mode
        }

        // Check whether the document exists -- this will also update `path` to update
        // a 'display name' to a 'document id' if the document exists.
        val fileExists = checkPathExists(path)

        // The docs for `CREATE_NEW` say that it takes precedence over `CREATE` if both are
        // present, so do that filtering now.
        val createMode = when {
            options.contains(StandardOpenOption.CREATE_NEW) -> {
                StandardOpenOption.CREATE_NEW
            }
            options.contains(StandardOpenOption.CREATE) -> {
                StandardOpenOption.CREATE
            }
            else -> {
                null
            }
        }

        // Does the file need to be created if it doesn't exist?
        if (createMode != null) {
            if (createMode == StandardOpenOption.CREATE_NEW && fileExists) {
                throw FileAlreadyExistsException(File(""), null, "$path already exists")
            } else if (!fileExists) {
                val displayName = path.docId!!
                val parent = path.parent!! as DocumentPath

                // For now, guess the mime type based on the file extension.
                val mimeType = if (displayName.contains('.')) {
                    val ext = displayName.substringAfterLast('.')
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                        ?: "application/octet-stream"
                } else {
                    "application/octet-stream"
                }

                // Actually create the document
                val newDocumentUri = DocumentsContract.createDocument(
                    context.contentResolver,
                    parent.androidUri,
                    mimeType,
                    displayName
                )

                // Update the path to use the document id rather than display name
                path.updateDocId(DocumentsContract.getDocumentId(newDocumentUri))
            }
        }

        val androidUri = path.androidUri
        return context.contentResolver.openFileDescriptor(androidUri, openMode)?.let { fd ->
            FileDescriptorChannel(fd.fileDescriptor)
        } ?: throw FileNotFoundException("openFileDescriptor($androidUri) returned null")
    }

    override fun newDirectoryStream(
        path: DocumentPath,
        filter: Filter<in Path>?
    ): DirectoryStream<Path> {
        val rootUri = path.androidUri

        val childDocsUri = path.childDocumentsUri
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            childDocsUri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
            null,
            null,
            null,
            null
        ) ?: throw FileNotFoundException("No files found for path $rootUri")

        val cursorSequence = generateSequence {
            if (cursor.moveToNext()) {
                path.resolve(cursor.getString(0))
            } else {
                cursor.close()
                null
            }
        }
        return SequenceDocumentDirectoryStream(cursorSequence)
    }

    override fun <A : BasicFileAttributes?> readAttributes(
        path: DocumentPath,
        type: Class<A>?,
        vararg options: LinkOption?
    ): A {
        try {
            val attributes = when (type) {
                BasicFileAttributes::class.java -> buildDocumentBasicAttributes(path)
                DocumentBasicAttributes::class.java -> buildDocumentBasicAttributes(path)
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

    private fun buildDocumentBasicAttributes(path: DocumentPath): BasicFileAttributes {
        context.contentResolver.query(
            path.androidUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE
            ),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToNext()) {
                val lastModifiedTime = FileTime.fromMillis(cursor.getLong(0))
                val mimeType = cursor.getString(1)
                val size = cursor.getLong(2)
                val isFolder = mimeType == DocumentsContract.Document.MIME_TYPE_DIR ||
                    mimeType == DocumentsContract.Root.MIME_TYPE_ITEM
                return DocumentBasicAttributes(
                    lastModifiedTime,
                    mimeType,
                    size,
                    isFolder
                )
            } else {
                // No such file?
                throw FileNotFoundException("File for ${path.androidUri}")
            }
        }

        // Couldn't read the attributes
        throw IOException("Could not query ContentResolver for ${path.androidUri}")
    }

    /**
     * Checking if a path exists requires 2 different checks.
     *
     * The first, faster check, is to construct the android.net.Uri representation of the Path
     * and try to open it. If it succeeds, then the file exists.
     *
     * If trying to open the file _fails_, however, the file might still exist, because the path
     * might be constructed with [Path.resolve] or [Path.resolveSibling] with a "display name",
     * rather than a document id.
     *
     * If the path has a display name, then we can query the children of the parent to see if the
     * display name matches one of the children. If it does, then we can update the `Path` to
     * include the document id, rather than its display name, in addition to noting that, yes,
     * the path exists.
     */
    private fun checkPathExists(path: DocumentPath): Boolean {
        val quickCheckUri = path.androidUri
        try {
            context.contentResolver.openFileDescriptor(quickCheckUri, "r")?.use {
                // If we're able to open it, the path exists
                return true
            }
        } catch (_: FileNotFoundException) {
            // Promising, but should continue to check that it isn't a display name
        } catch (_: SecurityException) {
            // Probably tried to build a URI based on a display name rather than a doc id
        }

        // If this is a display name at the end, then it should have a parent. If it doesn't,
        // then we can just assume it is a document ID and that the file really doesn't exist.
        if (path.parent == null) return false

        // We thought it was the document id, but it's probably a display name
        val filename = path.docId
        val queryUri = (path.parent as DocumentPath).childDocumentsUri
        try {
            context.contentResolver.query(
                queryUri,
                arrayOf(COLUMN_DOCUMENT_ID, COLUMN_DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val (docId, name) = cursor.getString(0) to cursor.getString(1)
                    if (name == filename) {
                        path.updateDocId(docId)
                        return true
                    }
                }
            }
        } finally {
            // Something went wrong
        }

        // If we get here, then the file probably doesn't exist
        return false
    }
}

/** @return [Uri] representation of this path. */
internal val DocumentPath.androidUri: Uri
    get() {
        val authority = fileSystem.authority
        val documentId = docId
        return if (treeId != null) {
            val treeUri = DocumentsContract.buildTreeDocumentUri(authority, treeId)
            if (documentId != null) {
                DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
            } else {
                treeUri
            }
        } else {
            DocumentsContract.buildDocumentUri(authority, documentId!!)
        }
    }

/**
 * @return Uri that can be queried to get a list of child documents (if any).
 */
internal val DocumentPath.childDocumentsUri: Uri
    get() {
        val uri = androidUri
        return if (treeId != null) {
            DocumentsContract.buildChildDocumentsUriUsingTree(
                uri, DocumentsContract.getDocumentId(uri)
            )
        } else {
            DocumentsContract.buildChildDocumentsUri(
                uri.authority, DocumentsContract.getDocumentId(uri)
            )
        }
    }
