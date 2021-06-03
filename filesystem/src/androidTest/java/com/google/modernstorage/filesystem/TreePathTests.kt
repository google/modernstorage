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

package com.google.modernstorage.filesystem

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.modernstorage.filesystem.provider.TestDocumentProvider
import com.google.modernstorage.filesystem.provider.document
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.nio.file.Files

class TreePathTests {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val testUri =
        Uri.parse("content://${context.packageName}.documents/tree/root")

    private val testRoot = document("root") {
        children {
            document("test-1.txt")
            document("test-2.txt")
            document("test-3.txt")
            document("subdir") {
                children {
                    document("child1.txt")
                    document("child2.txt")
                    document("child3.txt")
                }
            }
        }
    }

    @Before
    fun setup() {
        AndroidFileSystems.initialize(context)
        TestDocumentProvider.addRoot(testRoot)
    }

    @After
    fun teardown() {
        TestDocumentProvider.clearAll()
    }

    @Test
    fun filesWalk_FlatDirectory() {
        val expectedDocuments = mutableSetOf(
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Ftest-2.txt",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Ftest-3.txt",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir%2Fchild2.txt",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir%2Fchild3.txt"
        )

        val testPath = AndroidPaths.get(testUri)
        val directoryStream = Files.walk(testPath)
        directoryStream.forEach { document ->
            val docUri = document.toUri().toString()
            if (expectedDocuments.contains(docUri)) {
                expectedDocuments.remove(docUri)
            } else {
                Assert.fail("Unexpected URI: $docUri")
            }
        }

        // If we visited each document, then the set should be empty now
        Assert.assertTrue("Didn't visit all documents", expectedDocuments.isEmpty())
    }
}