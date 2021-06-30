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

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.modernstorage.filesystem.provider.TestDocumentProvider
import com.google.modernstorage.filesystem.provider.document
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.FileNotFoundException
import java.nio.file.Files

class DocumentPathTests {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val testUri =
        Uri.parse("content://${context.packageName}.documents/document/test.txt")
    private val testNonExistingUri =
        Uri.parse("content://${context.packageName}.documents/document/MISSING_FILE")

    private val testFileContent = listOf(
        "This is a test file.",
        "It contains two lines of text."
    )

    private val testRoot = document("test.txt") {
        content { testFileContent.joinToString("\n") }
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
    fun readSingleDocument_documentExists() {
        val testPath = AndroidPaths.get(testUri)
        val lines = Files.readAllLines(testPath)
        lines.forEachIndexed { line, text ->
            Assert.assertEquals(testFileContent[line], text)
        }
    }

    @Test
    fun readSingleDocument_documentDoesNotExist() {
        val testPath = AndroidPaths.get(testNonExistingUri)
        try {
            val lines = Files.readAllLines(testPath)
            Assert.fail("Opened non-existing file?")
        } catch (fileNotFound: FileNotFoundException) {
            // Test pass
        }
    }
}
