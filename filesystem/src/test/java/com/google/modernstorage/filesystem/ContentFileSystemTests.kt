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

import android.provider.DocumentsContract.Document.MIME_TYPE_DIR
import com.google.modernstorage.filesystem.internal.TestContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.URI
import java.nio.file.Files

/**
 * Unit tests for [ContentFileSystemProvider], and [ContentFileSystem].
 */
class ContentFileSystemTests {
    private lateinit var fileSystem: ContentFileSystem
    private lateinit var contract: TestContract

    @Before
    fun setup() {
        contract = TestContract()
        AndroidFileSystems.initialize(contract)
        val testUri = URI("content://unit.test/tree/root")
        fileSystem = AndroidFileSystems.getFileSystem(testUri) as ContentFileSystem
    }

    @Test
    fun testGetRootDirectories_registeredUris() {
        val roots = listOf(
            DocumentPath(fileSystem, "primary"),
            DocumentPath(fileSystem, "secondary")
        )

        // Request a series of paths
        listOf(
            listOf("primary", "FolderA"),
            listOf("primary", "FolderB"),
            listOf("primary", "FolderB", "Document"),
            listOf("secondary", "Document")
        ).forEach { pathData ->
            fileSystem.getPath(pathData[0], *pathData.subList(1, pathData.size).toTypedArray())
        }

        val expectedRoots = roots.toMutableSet()

        // Check all roots are registered
        fileSystem.rootDirectories.forEach { rootPath ->
            if (!expectedRoots.remove(rootPath)) {
                fail("Root found that wasn't registered: ${rootPath.toUri()}")
            }
        }
        assertTrue("Not all roots enumerated", expectedRoots.isEmpty())
    }

    @Test
    fun createFile_callsPlatform() {
        val path = DocumentPath(fileSystem, "tree", "parent", "child.txt")

        // Create depends on a few functions
        contract.existsImpl = { _ -> false }
        contract.openByteChannelImpl = { _, _ ->
            val tempFile = File.createTempFile("unit", "test").apply {
                deleteOnExit()
            }.toPath()
            Files.newByteChannel(tempFile)
        }

        // When the test is run, the parameter should be the path and a null mime type, since
        // mime type resolution is platform dependent.
        contract.createDocumentImpl = { createPath, mimeType ->
            assertSame(path, createPath)
            assertNull(mimeType)
            true
        }

        // Ask the OpenJDK to create the document.
        Files.createFile(path)
    }

    @Test
    fun createDirectory_callsPlatform() {
        val path = DocumentPath(fileSystem, "tree", "parent", "childDir")

        // Create depends on a few functions
        contract.existsImpl = { _ -> false }

        // When the test is run, the parameter should be the path and a null mime type, since
        // mime type resolution is platform dependent.
        contract.createDocumentImpl = { createPath, mimeType ->
            assertSame(path, createPath)
            assertEquals(MIME_TYPE_DIR, mimeType)
            true
        }

        // Ask the OpenJDK to create the directory.
        Files.createDirectory(path)
    }

    @Test
    fun isSameFileTests_sameFiles() {
        val pathA = DocumentPath(fileSystem, "tree", "parent", "childDir")
        val pathB = DocumentPath(fileSystem, "tree", "parent", "childDir")

        // Paths are the same, so it's the same file
        assertTrue(Files.isSameFile(pathA, pathB))

        // Paths point to the same document, but one is the 'absolute path'
        val pathC = DocumentPath(fileSystem, "tree", "parent", "child.txt")
        val pathD = DocumentPath(fileSystem, "tree", "grandparent", "parent", "child.txt")
        assertTrue(Files.isSameFile(pathC, pathD))
    }

    @Test
    fun isSameFileTests_failCases() {
        val testUriA = URI("content://unit.test.a/tree/root")
        val fsA = AndroidFileSystems.getFileSystem(testUriA) as ContentFileSystem
        val testUriB = URI("content://unit.test.b/tree/root")
        val fsB = AndroidFileSystems.getFileSystem(testUriB) as ContentFileSystem

        val pathA = DocumentPath(fsA, "tree", "parent", "child")
        val pathB = DocumentPath(fsB, "tree", "parent", "child")
        val pathB2 = DocumentPath(fsB, "tree2", "parent", "child")

        // Paths from different providers is false
        assertFalse(Files.isSameFile(pathA, pathB))

        // Paths from same provider, different trees
        assertFalse(Files.isSameFile(pathB, pathB2))
    }
}
