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
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.nio.file.Files


class SinglePathTests {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val testFile = File(context.filesDir, "test.txt")
    private val testUri =
        Uri.parse("content://com.google.modernstorage.test.documents/document/${testFile.name}")
    private val testNonExistingUri =
        Uri.parse("content://com.google.modernstorage.test.documents/document/MISSING_FILE")

    private val testFileContent = listOf(
        "This is a test file.",
        "It contains two lines of text."
    )

    @Before
    fun setup() {
        AndroidFileSystems.initialize(context)
        if (!testFile.exists()) {
            FileWriter(testFile).use { fileWriter ->
                testFileContent.forEach { line -> fileWriter.write("$line\n") }
            }
        }
    }

    @After
    fun teardown() {
        if (testFile.exists()) {
            testFile.delete()
        }
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