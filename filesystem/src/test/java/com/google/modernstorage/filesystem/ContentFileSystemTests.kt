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

    @Before
    fun setup() {
        AndroidFileSystems.initialize(TestContract())
    }

    @Test
    fun testGetRootDirectories_registeredUris() {
        val roots = listOf(
            "content://com.android.externalstorage.documents/tree/primary%3ATest",
            "content://com.android.externalstorage.documents/tree/primary%3ADocuments/One",
            "content://com.android.externalstorage.documents/tree/primary%3ADocuments/Two",
        ).map { URI(it) }

        val expectedRoots = roots.map { root ->
            // Register each root
            AndroidFileSystems.getFileSystem(root)
            // Get a ContentPath representation (to check later)
            AndroidPaths.get(root)
        }.toMutableSet()

        // Get the ContentFileSystem for the provider
        val fileSystem = AndroidFileSystems.getFileSystem(roots[0])

        // Check all roots are registered
        fileSystem.rootDirectories.forEach { rootPath ->
            if (!expectedRoots.remove(rootPath)) {
                fail("Root found that wasn't registered: ${rootPath.toUri()}")
            }
        }
        expectedRoots.forEach { root -> println("Root remains: $root") }
        assertTrue("Not all roots enumerated", expectedRoots.isEmpty())
    }
}