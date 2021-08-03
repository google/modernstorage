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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.URI
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

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

        // Setup test root and support `findDocumentPath` by default.
        TestDocumentProvider.addRoot(testRoot)
        TestDocumentProvider.supportFindDocumentPath = true
    }

    @After
    fun teardown() {
        TestDocumentProvider.clearAll()
    }

    @Test
    fun filesWalk_FlatDirectory() {
        val expectedDocuments = mutableSetOf(
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Ftest-1.txt",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Ftest-2.txt",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Ftest-3.txt",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir%2Fchild1.txt",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir%2Fchild2.txt",
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir%2Fchild3.txt"
        )

        val testPath = AndroidPaths.get(testUri)
        val directoryStream = Files.walk(testPath)
        directoryStream.forEach { document ->
            val docUri = document.toUri().toString()
            if (!expectedDocuments.remove(docUri)) {
                fail("Unexpected URI: $docUri")
            }
        }

        // If we visited each document, then the set should be empty now
        assertTrue("Didn't visit all documents", expectedDocuments.isEmpty())
    }

    @Test
    fun testPath_absolutePath() {
        val uri =
            URI("content://${context.packageName}.documents/tree/root/document/root%2Fsubdir%2Fchild1.txt")

        val fileSystem = AndroidFileSystems.getFileSystem(uri) as ContentFileSystem
        val expectedParts = listOf(
            DocumentPath(fileSystem, "root", "root"),
            DocumentPath(fileSystem, "root", "root/subdir"),
            DocumentPath(fileSystem, "root", "root/subdir/child1.txt")
        )

        val path = AndroidPaths.get(uri)
        val absolutePath = path.toAbsolutePath()
        val pathParts = absolutePath.toList()
        pathParts.forEachIndexed { index, part ->
            Assert.assertEquals(expectedParts[index], part)
        }
    }

    @Test
    fun testPath_absolutePathAlreadyAbsolute() {
        val uri =
            URI("content://${context.packageName}.documents/tree/root/document/root%2Fsubdir%2Fchild1.txt")

        val fileSystem = AndroidFileSystems.getFileSystem(uri) as ContentFileSystem
        val expected =
            DocumentPath(
                fileSystem,
                "root",
                "root",
                "root/subdir",
                "root/subdir/child1.txt"
            ).toAbsolutePath()
        Assert.assertSame(expected, expected.toAbsolutePath())
    }

    @Test
    fun testPath_absolutePathFindPathUnsupportedReturnsSelf() {
        val uri =
            URI("content://${context.packageName}.documents/tree/root/document/root%2Fsubdir%2Fchild1.txt")

        // Tell our DocumentsProvider not to support `findDocumentPath`
        TestDocumentProvider.supportFindDocumentPath = false

        val fileSystem = AndroidFileSystems.getFileSystem(uri) as ContentFileSystem
        val expected =
            DocumentPath(fileSystem, "root", "root", "root/subdir", "root/subdir/child1.txt")
        Assert.assertSame(expected, expected.toAbsolutePath())
    }

    @Test
    fun createNewDocument() {
        val docUri =
            Uri.parse("content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir%2Fchild1.txt")
        val existingPath = AndroidPaths.get(docUri).toAbsolutePath()
        val newPath = existingPath.resolveSibling("child_new.txt")
        Files.createFile(newPath)
    }

    @Test
    fun createNewDocument_failsIfExistsDisplayName() {
        val docUri =
            Uri.parse("content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir%2Fchild1.txt")
        val basePath = AndroidPaths.get(docUri).toAbsolutePath()
        val existingPath = basePath.resolveSibling("child2.txt")
        try {
            Files.createFile(existingPath)
            fail()
        } catch (_: FileAlreadyExistsException) {
            // Test pass!
            println("Pass! $existingPath")
        }
    }

    @Test
    fun openDirectoryAsFile() {
        val dirUri =
            Uri.parse("content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir")
        val path = AndroidPaths.get(dirUri)
        try {
            Files.readAllLines(path).forEach { println(it) }
            fail()
        } catch (ioe: IOException) {
            // Pass
        }
    }

    @Test
    fun removeDocument() {
        val docUri =
            Uri.parse("content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir%2Fchild1.txt")
        val existingPath = AndroidPaths.get(docUri).toAbsolutePath()
        Files.delete(existingPath)
        assertFalse(Files.exists(existingPath))
    }

    @Test
    fun removeDocument_doesNotExist() {
        val dirUri =
            Uri.parse("content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root%2Fsubdir%2Fnon-existing.txt")
        val path = AndroidPaths.get(dirUri)
        Files.deleteIfExists(path)
    }

    @Test
    fun createDirectory() {
        val dirUri =
            Uri.parse("content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root")
        val path = AndroidPaths.get(dirUri).toAbsolutePath()
        val newDirPath = path.resolve("TestDir")
        Files.createDirectory(newDirPath)
        assertTrue(Files.exists(newDirPath))
    }

    @Test
    fun createDirectories() {
        val uriBase =
            "content://com.google.modernstorage.filesystem.test.documents/tree/root/document/root"
        val dirUri =
            Uri.parse(uriBase)
        val path = AndroidPaths.get(dirUri).toAbsolutePath()
        val newDirs = path.resolve("TestDir").resolve("SubTest")
        Files.createDirectories(newDirs)

        val expectedNewDirs = listOf(
            AndroidPaths.get(Uri.parse("$uriBase%2FTestDir")).toAbsolutePath(),
            AndroidPaths.get(Uri.parse("$uriBase%2FTestDir%2FSubTest")).toAbsolutePath()
        )

        // Now that each of those directories should have been created, we can verify that
        expectedNewDirs.forEach { dir ->
            val attrs = Files.readAttributes(dir, BasicFileAttributes::class.java)
            assertTrue(attrs.isDirectory)
        }
    }
}
