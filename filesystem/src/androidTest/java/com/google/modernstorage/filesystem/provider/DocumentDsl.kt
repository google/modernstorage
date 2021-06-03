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

import android.provider.DocumentsContract.Document
import android.webkit.MimeTypeMap

class TestDocument(
    val docId: String,
    val displayName: String = docId,
    val content: String?,
    val children: List<TestDocument>?
) {
    val mimeType
        get() = when {
            children?.isNotEmpty() == true -> {
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

class TestDocumentBuilder(private val docId: String) {
    var displayName: String? = null
    private var content: String? = null
    private val children = mutableMapOf<String, TestDocument>()

    fun children(block: ChildBuilder.() -> Unit) {
        val childDocuments = ChildBuilder().apply(block).build()
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
        docId,
        displayName ?: docId,
        content,
        if (children.isEmpty()) null else children.values.toList()
    )
}

class ChildBuilder() {
    private val children = mutableMapOf<String, TestDocument>()

    fun document(docId: String, block: TestDocumentBuilder.() -> Unit = { }) {
        val childDocument = TestDocumentBuilder(docId).apply {
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

private fun test() {
    document("root") {
        displayName = "Root"
        children {
            document("test.txt") {
                content { "This is the test document" }
            }
        }
    }
}