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
package com.google.modernstorage.filesystem.provider

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.parseMode
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.lang.IllegalStateException

class TestDocumentProvider : DocumentsProvider() {

    companion object {
        private val testRoots = mutableListOf<TestDocument>()
        private val docIdsToDoc = mutableMapOf<String, TestDocument>()
        private val docIdsToFile = mutableMapOf<String, File>()
        var supportFindDocumentPath = true

        fun clearAll() {
            testRoots.clear()
            docIdsToDoc.clear()
        }

        fun addRoot(root: TestDocument) {
            if (root !in testRoots) {
                testRoots += root
                updateMap(root)
            }
        }

        fun getFile(documentId: String?) = docIdsToFile[documentId]

        private fun updateMap(doc: TestDocument) {
            if (docIdsToDoc[doc.docId] == null) {
                docIdsToDoc[doc.docId] = doc
            }
            if (doc.children.isNotEmpty()) {
                doc.children.forEach { childDocument ->
                    updateMap(childDocument)
                }
            }
        }
    }

    private val defaultRootProjection = arrayOf(
        Root.COLUMN_ROOT_ID,
        Root.COLUMN_SUMMARY,
        Root.COLUMN_FLAGS,
        Root.COLUMN_MIME_TYPES,
        Root.COLUMN_TITLE,
        Root.COLUMN_DOCUMENT_ID,
        Root.COLUMN_AVAILABLE_BYTES
    )

    private val defaultDocumentProjection = arrayOf(
        Document.COLUMN_DOCUMENT_ID,
        Document.COLUMN_MIME_TYPE,
        Document.COLUMN_DISPLAY_NAME,
        Document.COLUMN_LAST_MODIFIED,
        Document.COLUMN_FLAGS,
        Document.COLUMN_SIZE
    )

    override fun onCreate() = true

    override fun queryRoots(projection: Array<out String>?): Cursor {
        val useProjection = projection ?: defaultRootProjection
        val cursor = MatrixCursor(useProjection)

        testRoots.forEach { root ->
            // Add row for the root document/directory
            cursor.newRow().apply {
                add(Root.COLUMN_ROOT_ID, root.docId)
                add(Root.COLUMN_SUMMARY, "")
                add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE or Root.FLAG_SUPPORTS_IS_CHILD)
                add(Root.COLUMN_TITLE, "TestProvider")
                add(Root.COLUMN_DOCUMENT_ID, root.docId)
                add(Root.COLUMN_MIME_TYPES, "*/*")
                add(Root.COLUMN_AVAILABLE_BYTES, null)
            }
        }

        return cursor
    }

    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        val document = docIdsToDoc[documentId] ?: throw FileNotFoundException()

        val useProjection = projection ?: defaultDocumentProjection
        val cursor = MatrixCursor(useProjection)
        cursor.newRow().apply {
            add(Document.COLUMN_DOCUMENT_ID, document.docId)
            add(Document.COLUMN_MIME_TYPE, document.mimeType)
            add(Document.COLUMN_DISPLAY_NAME, document.displayName)
            add(Document.COLUMN_LAST_MODIFIED, 0)
            add(Document.COLUMN_FLAGS, 0)
            add(Document.COLUMN_SIZE, document.size)
        }
        return cursor
    }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val parentDocument = docIdsToDoc[parentDocumentId] ?: throw FileNotFoundException()
        val useProjection = projection ?: defaultRootProjection

        val cursor = MatrixCursor(useProjection)
        parentDocument.children.forEach { childDocument ->
            cursor.newRow().apply {
                add(Document.COLUMN_DOCUMENT_ID, childDocument.docId)
                add(Document.COLUMN_MIME_TYPE, childDocument.mimeType)
                add(Document.COLUMN_DISPLAY_NAME, childDocument.displayName)
                add(Document.COLUMN_LAST_MODIFIED, 0)
                add(Document.COLUMN_FLAGS, 0)
                add(Document.COLUMN_SIZE, childDocument.size)
            }
        }

        return cursor
    }

    override fun isChildDocument(parentDocumentId: String?, documentId: String?): Boolean {
        parentDocumentId ?: return false
        documentId ?: return false
        return documentId.startsWith(parentDocumentId)
    }

    override fun findDocumentPath(
        parentDocumentId: String?,
        childDocumentId: String?
    ): DocumentsContract.Path? {
        // If we don't support this, the super class throws.
        if (!supportFindDocumentPath) {
            return super.findDocumentPath(parentDocumentId, childDocumentId)
        }

        val path = mutableListOf<String>()
        var child: TestDocument? = docIdsToDoc[childDocumentId]
        while (child != null) {
            path.add(child.docId)
            child = docIdsToDoc[child.parentDocId]
        }
        return if (path.isEmpty()) null else DocumentsContract.Path(null, path.reversed())
    }

    /**
     * A very naive implementation of [createDocument].
     *
     * This doesn't handle creating two documents with the same [displayName] like a standard
     * [DocumentsProvider] would. For tests this isn't as big of a problem, but could be added.
     */
    override fun createDocument(
        parentDocumentId: String?,
        mimeType: String?,
        displayName: String?
    ): String {
        val parent = docIdsToDoc[parentDocumentId] ?: throw FileNotFoundException()
        val docId = "$parentDocumentId/$displayName"
        val newDoc = TestDocument(docId, parentDocumentId)
        parent.children.add(newDoc)
        docIdsToDoc[docId] = newDoc
        return docId
    }

    /**
     * This implementation of [openDocument] only supports reading from documents.
     * TODO: Support writing so it's possible to test writing to paths.
     */
    override fun openDocument(
        documentId: String,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        val document = docIdsToDoc[documentId] ?: throw FileNotFoundException()

        val file = docIdsToFile[documentId]
            ?: if (document.children.isEmpty()) {
                File.createTempFile("tdp", null, context!!.cacheDir).also { tmp ->
                    if (!mode.contains('t')) {
                        FileWriter(tmp).write(document.content)
                    }
                    tmp.deleteOnExit()
                }
            } else {
                // It's actually a directory, so just use one that exists... (it's an error anyway)
                context!!.filesDir
            }
        docIdsToFile[documentId] = file
        return ParcelFileDescriptor.open(file, parseMode(mode))
    }

    /**
     * Bare-bones implementation of [removeDocument] -- simply removes the entry from the map,
     * since that's the only way to find it. :)
     */
    override fun removeDocument(documentId: String?, parentDocumentId: String?) {
        val parent = docIdsToDoc[parentDocumentId] ?: throw FileNotFoundException()
        val document = docIdsToDoc[documentId] ?: throw FileNotFoundException()
        if (document !in parent.children) throw FileNotFoundException()

        // Checks done -- remove it
        parent.children.remove(document)
        docIdsToDoc.remove(documentId)
    }

    /**
     * Even more bare-bones. This should only ever be called on a document that doesn't exist,
     * since documents that _do_ exist would have a parent, and the code should call through to
     * [removeDocument] instead.
     */
    override fun deleteDocument(documentId: String?) {
        docIdsToDoc[documentId] ?: throw FileNotFoundException()
        throw IllegalStateException("deleteDocument called instead of removeDocument!")
    }
}
