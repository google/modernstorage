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

import com.google.modernstorage.filesystem.internal.TestContract
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.net.URI

/**
 * Unit tests for [ContentFileSystemProvider], and [ContentFileSystem].
 */
class ContentFileSystemTests {
    private lateinit var fileSystem: ContentFileSystem

    @Before
    fun setup() {
        AndroidFileSystems.initialize(TestContract())
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
}
