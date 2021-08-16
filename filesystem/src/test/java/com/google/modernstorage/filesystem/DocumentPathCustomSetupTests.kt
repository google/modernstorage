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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

class DocumentPathCustomSetupTests {

    @Test
    fun testAbsolutePath_callsThrough() {
        // A relative path should be able to be resolved
        val contract = TestContract(
            findDocumentPathImpl = { listOf("tree", "parent", "child.txt") }
        )
        AndroidFileSystems.initialize(contract)

        val testUri = URI("content://unit.test/tree/root")
        val fileSystem = AndroidFileSystems.getFileSystem(testUri) as ContentFileSystem

        // "child.txt" should get resolved to an absolute path
        val childPath = DocumentPath(fileSystem, "tree", "child.txt")
        val expectedAbsolute = DocumentPath(fileSystem, "tree", "tree", "parent", "child.txt")
        assertEquals(expectedAbsolute, childPath.toAbsolutePath())

        // If a path already is absolute, it should return itself
        contract.findDocumentPathImpl = { listOf("tree") }
        val startPath = DocumentPath(fileSystem, "tree", "tree", "parent", "child.txt")
        val absolutePath = startPath.toAbsolutePath()
        assertSame(absolutePath, absolutePath.toAbsolutePath())
    }

    @Test
    fun testAbsolutePath_isAbsolute() {
        // A relative path should be able to be resolved
        val contract = TestContract(
            findDocumentPathImpl = { listOf("tree", "parent", "child.txt") }
        )
        AndroidFileSystems.initialize(contract)

        val testUri = URI("content://unit.test/tree/root")
        val fileSystem = AndroidFileSystems.getFileSystem(testUri) as ContentFileSystem

        // childPath isn't absolute at first
        val childPath = DocumentPath(fileSystem, "tree", "child.txt")
        assertFalse(childPath.isAbsolute)
        // After resolved to an absolute path, it should be
        assertTrue(childPath.toAbsolutePath().isAbsolute)

        contract.findDocumentPathImpl = { listOf("tree") }
        val absolutePath = DocumentPath(fileSystem, "tree", "tree", "parent", "child.txt")
        // Even though the path _is_ absolute, it can't know that without doing file I/O
        assertFalse(absolutePath.isAbsolute)

        // But the new path (which mostly matches the old one) should know it's absolute
        assertTrue(absolutePath.toAbsolutePath().isAbsolute)
    }

    @Test
    fun testResolve_absolutePathStillAbsolute() {
        // A relative path should be able to be resolved
        val contract = TestContract(
            findDocumentPathImpl = { listOf("a") }
        )
        AndroidFileSystems.initialize(contract)

        val testUri = URI("content://unit.test/tree/root")
        val fileSystem = AndroidFileSystems.getFileSystem(testUri) as ContentFileSystem

        val absolutePath = DocumentPath(fileSystem, "tree", "a", "b", "c").toAbsolutePath()

        // Two different ways to call the resolve methods -- strings and Paths
        val resolveString = "d"
        val resolvePath = DocumentPath(fileSystem, "tree", "d")

        // Two ways to use [Path.resolve]:
        assertTrue(absolutePath.resolve(resolveString).isAbsolute)
        assertTrue(absolutePath.resolve(resolvePath).isAbsolute)

        // And two ways to resolveSibling
        assertTrue(absolutePath.resolveSibling(resolveString).isAbsolute)
        assertTrue(absolutePath.resolveSibling(resolvePath).isAbsolute)
    }

    @Test
    fun testResolve_absolutePathsStillAbsolute() {
        // A relative path should be able to be resolved
        val contract = TestContract(
            findDocumentPathImpl = { listOf("a") }
        )
        AndroidFileSystems.initialize(contract)

        val testUri = URI("content://unit.test/tree/root")
        val fileSystem = AndroidFileSystems.getFileSystem(testUri) as ContentFileSystem

        val absolutePath = DocumentPath(fileSystem, "tree", "a", "b", "c").toAbsolutePath()

        // The parent of an absolute path should still be absolute
        assertTrue(absolutePath.parent.isAbsolute)

        // The first name of an absolute path should likewise be absolute
        assertTrue(absolutePath.getName(0).isAbsolute)

        // Any other name should *not* be absolute though
        assertFalse(absolutePath.getName(1).isAbsolute)

        // Subpaths that start at index 0 should be absolute, while those that start after 0
        // should be relative.
        assertTrue(absolutePath.subpath(0, 1).isAbsolute)
        assertFalse(absolutePath.subpath(1, 2).isAbsolute)
    }
}
