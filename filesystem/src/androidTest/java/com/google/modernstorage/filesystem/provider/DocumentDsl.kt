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

import android.provider.DocumentsContract.Document
import android.webkit.MimeTypeMap

class TestDocument(
    val docId: String,
    val parentDocId: String?,
    val displayName: String = docId,
    val content: String? = null,
    val children: MutableList<TestDocument> = mutableListOf()
) {
    init {
        if (content != null && children.isNotEmpty()) {
            throw IllegalArgumentException("A document can not contain content and children at the same")
        }
    }

    val mimeType: String
        get() = when {
            children.isNotEmpty() -> {
                Document.MIME_TYPE_DIR
            }
            displayName.lastIndexOf(".") > 0 -> {
                MimeTypeMap.getFileExtensionFromUrl(docId)
            }
            else -> {
                "application/octet-stream"
            }
        }

    val size get() = content?.length ?: 0
}

class TestDocumentBuilder(private val docId: String, private val parentDocId: String? = null) {
    private var displayName: String? = null
    private var content: String? = null
    private val children = mutableMapOf<String, TestDocument>()
    private val fullDocId get() = if (parentDocId == null) docId else "$parentDocId/$docId"

    fun children(block: ChildBuilder.() -> Unit) {
        val childDocuments = ChildBuilder(fullDocId).apply(block).build()
        childDocuments.forEach { child ->
            if (children[child.docId] == null) {
                children[child.docId] = child
            }
        }
    }

    fun content(block: TestDocumentBuilder.() -> String) {
        content = block()
    }

    fun build() = TestDocument(
        fullDocId,
        parentDocId,
        displayName ?: docId,
        content,
        children.values.toMutableList()
    )
}

class ChildBuilder(private val parentDocId: String) {
    private val children = mutableMapOf<String, TestDocument>()

    fun document(docId: String, block: TestDocumentBuilder.() -> Unit = { }) {
        val childDocument = TestDocumentBuilder(docId, parentDocId).apply {
            block()
        }.build()
        if (children[childDocument.docId] == null) {
            children[childDocument.docId] = childDocument
        }
    }

    fun build() = children.values.toList()
}

fun document(docId: String, block: TestDocumentBuilder.() -> Unit = { }) =
    TestDocumentBuilder(docId).apply {
        block()
    }.build()
