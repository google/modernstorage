package com.google.modernstorage.filesystem.internal

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import com.google.modernstorage.filesystem.ContentPath
import com.google.modernstorage.filesystem.DocumentBasicAttributes
import com.google.modernstorage.filesystem.SequenceDocumentDirectoryStream
import com.google.modernstorage.filesystem.toURI
import com.google.modernstorage.filesystem.toUri
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.DirectoryStream
import java.nio.file.DirectoryStream.Filter
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

class AndroidContentContract(context: Context) : PlatformContract {
    private val context = context.applicationContext

    override val scheme = ContentResolver.SCHEME_CONTENT

    override fun isSupportedUri(uri: URI) = DocumentsContract.isDocumentUri(context, uri.toUri())

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

    override fun openByteChannel(uri: URI, mode: String): SeekableByteChannel {
        // Fix for https://issuetracker.google.com/180526528
        val openMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mode == "w") {
            "rwt"
        } else {
            mode
        }

        val androidUri = uri.toUri()
        return context.contentResolver.openFileDescriptor(androidUri, openMode)?.let { fd ->
            FileInputStream(fd.fileDescriptor).channel
        } ?: throw FileNotFoundException("openFileDescriptor($uri) returned null")
    }

    override fun newDirectoryStream(
        path: ContentPath,
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
            Log.d("nicole", "running sequence...")
            if (cursor.moveToNext()) {
                val documentId = cursor.getString(0)
                val childUri = if (path.isTree) {
                    DocumentsContract.buildDocumentUriUsingTree(rootUri, documentId)
                } else {
                    DocumentsContract.buildChildDocumentsUri(rootUri.authority, documentId)
                }
                Log.d("nicole", "yielding path: ${childUri.toURI()}")
                path.fileSystem.provider().getPath(childUri.toURI())
            } else {
                Log.d("nicole", "Cursor is empty")
                cursor.close()
                null
            }
        }
        return SequenceDocumentDirectoryStream(cursorSequence)
    }

    override fun <A : BasicFileAttributes?> readAttributes(
        path: ContentPath,
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

    private fun buildDocumentBasicAttributes(path: ContentPath): DocumentBasicAttributes {
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
}

/**
 * Useful extensions for [ContentPath]
 */
internal val ContentPath.androidUri get() = Uri.parse(toUri().toString())
internal val ContentPath.isTree get() = DocumentsContract.isTreeUri(androidUri)

/**
 * Uri that can be queried to get a list of child documents (if any).
 */
private val ContentPath.childDocumentsUri
    get() = if (isTree) {
        DocumentsContract.buildChildDocumentsUriUsingTree(
            androidUri,
            DocumentsContract.getDocumentId(androidUri)
        )
    } else {
        DocumentsContract.buildChildDocumentsUri(
            androidUri.authority,
            DocumentsContract.getDocumentId(androidUri)
        )
    }
