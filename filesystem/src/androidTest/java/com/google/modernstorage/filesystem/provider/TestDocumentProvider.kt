/*
 * Copyright 2021 The Android Open Source Project
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

package com.google.modernstorage.filesystem.provider

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.OutputStreamWriter
import kotlin.concurrent.thread

class TestDocumentProvider : DocumentsProvider() {

    companion object {
        private val testRoots = mutableListOf<TestDocument>()
        private val docIdsToDoc = mutableMapOf<String, TestDocument>()

        fun clearAll() {
            testRoots.clear()
            docIdsToDoc.clear()
        }

        fun addRoot(root: TestDocument) {
            if (root !in testRoots) {
                testRoots += root
                updateMap(null, root)
            }
        }

        private fun updateMap(root: String?, doc: TestDocument) {
            val fullDocId = if (root != null) "$root/${doc.docId}" else doc.docId
            if (docIdsToDoc[fullDocId] == null) {
                docIdsToDoc[fullDocId] = doc
            }
            if (doc.children?.isNotEmpty() == true) {
                doc.children.forEach { childDocument ->
                    updateMap(fullDocId, childDocument)
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

        // Add row for the root document/directory
        cursor.newRow().apply {
            add(Root.COLUMN_ROOT_ID, "root")
            add(Root.COLUMN_SUMMARY, "")
            add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE or Root.FLAG_SUPPORTS_IS_CHILD)
            add(Root.COLUMN_TITLE, "TestProvider")
            add(Root.COLUMN_DOCUMENT_ID, "")
            add(Root.COLUMN_MIME_TYPES, "*/*")
            add(Root.COLUMN_AVAILABLE_BYTES, null)
        }
        return cursor
    }

    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        documentId ?: throw FileNotFoundException()

        val file = if (documentId.startsWith("files/")) {
            Log.d("nicole", "Starts with files/ building directory without that: $documentId")
            File(requireContext().filesDir, documentId.substring("files/".length))
        } else {
            Log.d("nicole", "Looks like a direct path: $documentId")
            File(requireContext().filesDir, documentId)
        }
        throw IllegalArgumentException("doc: path='${file.absolutePath}', documentId='$documentId'")
    }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        throw IllegalStateException()
    }

    override fun isChildDocument(parentDocumentId: String?, documentId: String?): Boolean {
        val parentDocument = File(requireContext().filesDir, parentDocumentId!!)
        val childDocument = File(parentDocument, documentId!!)
        return childDocument.exists()
    }

    override fun openDocument(
        documentId: String?,
        mode: String?,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        val document = docIdsToDoc[documentId] ?: throw FileNotFoundException()
        val (readFd, writeFd) = ParcelFileDescriptor.createPipe()
        thread(name = "io") {
            val out = ParcelFileDescriptor.AutoCloseOutputStream(writeFd)
            OutputStreamWriter(out).apply { write((document.content)) }
        }
        return readFd
    }
}