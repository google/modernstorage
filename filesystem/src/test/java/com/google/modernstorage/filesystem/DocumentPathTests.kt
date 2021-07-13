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
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.net.URI

class DocumentPathTests {
    private lateinit var fileSystem: ContentFileSystem

    @Before
    fun setup() {
        AndroidFileSystems.initialize(TestContract())
        val testUri = URI("content://unit.test/tree/root")
        fileSystem = AndroidFileSystems.getFileSystem(testUri) as ContentFileSystem
    }

    @Test
    fun testDocumentPath_resolveFromRoot() {
        val root = DocumentPath(fileSystem, "tree")
        val child = root.resolve("child") as DocumentPath

        assertEquals(root.treeId, child.treeId)
        assertEquals("child", child.docId)
        assertTrue(child.path.size == 1)
    }

    @Test
    fun testDocumentPath_resolveFromParent() {
        val parent = DocumentPath(fileSystem, "tree", "grandparent", "parent")
        val child = parent.resolve("child") as DocumentPath

        assertEquals(parent.treeId, child.treeId)
        assertEquals("child", child.docId)

        val expectedParents = listOf("grandparent", "parent")
        expectedParents.forEachIndexed { index, expected ->
            assertEquals(expected, child.path[index])
        }
    }

    @Test
    fun testDocumentPath_testTreeIterator() {
        val path = DocumentPath(fileSystem, "tree", "grandparent", "parent", "child")
        val expectedElements = listOf(
            DocumentPath(fileSystem, "tree"),
            DocumentPath(fileSystem, "tree", "grandparent"),
            DocumentPath(fileSystem, "tree", "parent"),
            DocumentPath(fileSystem, "tree", "child"),
        )

        val pathList = mutableListOf<DocumentPath>()
        path.iterator().forEach { pathList.add(it as DocumentPath) }

        expectedElements.forEachIndexed { index, expected ->
            assertEquals(expected.treeId, pathList[index].treeId)
            assertEquals(expected.docId, pathList[index].docId)
            assertEquals(expected.path.size, pathList[index].path.size)
        }
    }

    @Test
    fun testDocumentPath_getParentFromChild() {
        val child = DocumentPath(fileSystem, "tree", "parent", "child")
        val parent = child.parent as DocumentPath

        assertEquals(child.treeId, parent.treeId)
        assertEquals("parent", parent.docId)
        assertTrue(parent.path.size == 1)
    }

    @Test
    fun testDocumentPath_getParentFromGrandChild() {
        val child = DocumentPath(fileSystem, "tree", "grandparent", "parent", "child")
        val parent = child.parent as DocumentPath

        assertEquals(child.treeId, parent.treeId)
        assertEquals("grandparent", parent.path[0])
        assertEquals("parent", parent.docId)
        assertTrue(parent.path.size == 2)
    }

    @Test
    fun testDocumentPath_getParentFromChildWithoutTree() {
        val child = DocumentPath(fileSystem, null, "grandparent", "parent", "child")
        val parent = child.parent as DocumentPath

        assertEquals(child.treeId, parent.treeId)
        assertEquals("parent", parent.docId)
        assertEquals("grandparent", parent.path[0])
        assertTrue(parent.path.size == 2)
    }

    @Test
    fun testDocumentPath_getParentFromRootWithoutTreeIsNull() {
        val child = DocumentPath(fileSystem, null, "child")
        assertNull(child.parent)
    }

    @Test
    fun testDocumentPath_getParentFromRootWithTreeAndDocumentIdIsNull() {
        val child = DocumentPath(fileSystem, "tree", "tree")
        assertNull(child.parent)
    }

    @Test
    fun testDocumentPath_getParentFromRootWithTreeIsNull() {
        val child = DocumentPath(fileSystem, "tree")
        assertNull(child.parent)
    }

    @Test
    fun testDocumentPath_getRootWihoutTreeIsNull() {
        val child = DocumentPath(fileSystem, null, "child")
        assertNull(child.root)
    }

    @Test
    fun testDocumentPath_getRootFromRootIsIdentical() {
        val root = DocumentPath(fileSystem, "tree")
        assertEquals(root, root.root)
    }

    @Test
    fun testDocumentPath_pathsFromDifferentProvidersNotEqual() {
        val providerA = URI("content://test.provider.a/tree/root")
        val fileSystemA = AndroidFileSystems.getFileSystem(providerA) as ContentFileSystem

        val providerB = URI("content://test.provider.b/tree/root")
        val fileSystemB = AndroidFileSystems.getFileSystem(providerB) as ContentFileSystem

        val pathA = DocumentPath(fileSystemA, "root", "parent", "child")
        val pathB = DocumentPath(fileSystemB, "root", "parent", "child")
        assert(pathA != pathB)
    }

    @Test
    fun testDocumentPath_filenameFromTreeWithoutFilename() {
        val root = DocumentPath(fileSystem, "tree")
        assertNull(root.fileName)
    }

    @Test
    fun testDocumentPath_filenameFromTreeWithFilename() {
        val root = DocumentPath(fileSystem, "tree", "filename")
        val filename = root.fileName as DocumentPath

        assertEquals(root.treeId, filename.treeId)
        assertEquals("filename", filename.docId)
        assertTrue(filename.path.size == 1)
    }

    @Test
    fun testDocumentPath_filenameFromDocument() {
        val root = DocumentPath(fileSystem, null, "filename")
        val filename = root.fileName as DocumentPath

        assertNull(filename.treeId)
        assertEquals("filename", filename.docId)
        assertTrue(filename.path.size == 1)
    }

    @Test
    fun testDocumentPath_filenameFromDocumentWithParents() {
        val root = DocumentPath(fileSystem, null, "grandparent", "parent", "filename")
        val filename = root.fileName as DocumentPath

        assertNull(filename.treeId)
        assertEquals("filename", filename.docId)
        assertTrue(filename.path.size == 1)
    }

    @Test
    fun testDocumentPath_filenameFromTreeWithParents() {
        val root = DocumentPath(fileSystem, null, "grandparent", "parent", "filename")
        val filename = root.fileName as DocumentPath

        assertEquals(root.treeId, filename.treeId)
        assertEquals("filename", filename.docId)
        assertTrue(filename.path.size == 1)
    }

    @Test
    fun testDocumentPath_compareAndEqualsTheSame() {
        val providerA = URI("content://test.provider.a/tree/root")
        val fileSystemA = AndroidFileSystems.getFileSystem(providerA) as ContentFileSystem

        val providerB = URI("content://test.provider.b/tree/root")
        val fileSystemB = AndroidFileSystems.getFileSystem(providerB) as ContentFileSystem

        val pathA = DocumentPath(fileSystemA, "root", "parent", "child")
        val pathB = DocumentPath(fileSystemB, "root", "parent", "child")

        val compare = pathA.compareTo(pathB) == 0
        val equals = pathA == pathB
        assert(compare == equals)
    }

    @Test
    fun testDocumentPath_compareAuthoritiesFirst() {
        val providerA = URI("content://test.provider.a/tree/root")
        val fileSystemA = AndroidFileSystems.getFileSystem(providerA) as ContentFileSystem

        val providerB = URI("content://test.provider.b/tree/root")
        val fileSystemB = AndroidFileSystems.getFileSystem(providerB) as ContentFileSystem

        val pathA = DocumentPath(fileSystemA, "root", "parent", "child")
        val pathB = DocumentPath(fileSystemB, "root", "parent", "child")

        assert(pathA < pathB)
        assert(pathB > pathA)
    }

    @Test
    fun testDocumentPath_compareIgnoresParents() {
        val pathA = DocumentPath(fileSystem, "root", "cupcake", "abcd")
        val pathB = DocumentPath(fileSystem, "root", "donut", "zyxw")
        val pathC = DocumentPath(fileSystem, "root", "cupcake", "donut", "abcd")

        assert(pathA < pathB)
        assert(pathB > pathA)
        assert(pathA.compareTo(pathC) == 0)
    }

    @Test
    fun testDocumentPath_getName() {
        val path = DocumentPath(fileSystem, "tree", "grandparent", "parent", "child")
        val expectedElements = listOf(
            DocumentPath(fileSystem, "tree", "grandparent"),
            DocumentPath(fileSystem, "tree", "parent"),
            DocumentPath(fileSystem, "tree", "child"),
        )

        assertEquals(3, path.nameCount)
        for (index in 0 until path.nameCount) {
            val name = path.getName(index) as DocumentPath
            assertEquals(expectedElements[index].treeId, name.treeId)
            assertEquals(expectedElements[index].docId, name.docId)
            assertEquals(expectedElements[index].path.size, name.path.size)
        }

        try {
            val outOfBoundsName = path.getName(path.nameCount + 1)
            fail("Read name beyond nameCount: $outOfBoundsName")
        } catch (_: IndexOutOfBoundsException) {
            // Expected exception
        }
    }

    @Test
    fun testDocumentPath_relativizeDifferentProviders() {
        val providerA = URI("content://test.provider.a/tree/root")
        val fileSystemA = AndroidFileSystems.getFileSystem(providerA) as ContentFileSystem

        val providerB = URI("content://test.provider.b/tree/root")
        val fileSystemB = AndroidFileSystems.getFileSystem(providerB) as ContentFileSystem

        val pathA = DocumentPath(fileSystemA, "root", "parent", "child")
        val pathB = DocumentPath(fileSystemB, "root", "parent", "child")

        try {
            val relativePath = pathA.relativize(pathB)
            fail()
        } catch (_: IllegalArgumentException) {
            // Test pass
        }
    }

    @Test
    fun testDocumentPath_relativizeDifferentRoots() {
        val pathA = DocumentPath(fileSystem, "root1", "cupcake", "abcd")
        val pathB = DocumentPath(fileSystem, "root2", "donut", "zyxw")

        try {
            val relativePath = pathA.relativize(pathB)
            fail()
        } catch (_: IllegalArgumentException) {
            // Test pass
        }
    }

    @Test
    fun testDocumentPath_relativizeDown() {
        /*
         * Unix path behavior:
         * "/grandparent/parent".relativize("/grandparent/parent/child") -> "child"
         */
        val pathA = DocumentPath(fileSystem, "tree", "grandparent", "parent")
        val pathB = DocumentPath(fileSystem, "tree", "grandparent", "parent", "child")

        val expected = DocumentPath(fileSystem, "tree", "child")
        val relative = pathA.relativize(pathB) as DocumentPath
        assertDeepEquals(expected, relative)
    }

    @Test
    fun testDocumentPath_relativizeUp() {
        /*
         * Unix path behavior:
         * "/grandparent/parent/child".relativize("/grandparent/parent") -> ".."
         *
         * Because our path is built as a list of document ids, which are supposed to be treated
         * as opaque tokens, and because there's no restriction on the format of a document ids
         * this seems like a difficult situation.
         *
         * To solve it, we'll make an implementation specific "parent" document id to stand in
         * for the ".." on a Unix file system.
         */
        val pathA = DocumentPath(fileSystem, "tree", "grandparent", "parent")
        val pathB = DocumentPath(fileSystem, "tree", "grandparent", "parent", "child")

        val expected = DocumentPath(fileSystem, "tree", RELATIVE_PARENT_ID)
        val relative = pathB.relativize(pathA) as DocumentPath
        assertDeepEquals(expected, relative)
    }

    @Test
    fun testDocumentPath_relativizeSameIsEmpty() {
        /*
         * Unix path behavior:
         * "/grandparent/parent/child".relativize("/grandparent/parent/child") -> ""
         */
        val pathA = DocumentPath(fileSystem, "tree", "grandparent", "parent", "child")
        val pathB = DocumentPath(fileSystem, "tree", "grandparent", "parent", "child")

        val expected = DocumentPath(fileSystem, "tree")
        val relative = pathA.relativize(pathB) as DocumentPath
        assertDeepEquals(expected, relative)
    }

    @Test
    fun testDocumentPath_relativizeNothingCommon() {
        /*
         * Unix path behavior:
         * "/a/b/c".relativize("/x/y/z") = "../../../x/y/z"
         */
        val pathA = DocumentPath(fileSystem, "tree", "a", "b", "c")
        val pathB = DocumentPath(fileSystem, "tree", "x", "y", "z")

        val expected = DocumentPath(
            fileSystem,
            "tree",
            RELATIVE_PARENT_ID, RELATIVE_PARENT_ID, RELATIVE_PARENT_ID, "x", "y", "z"
        )
        val relative = pathA.relativize(pathB) as DocumentPath
        assertDeepEquals(expected, relative)
    }

    @Test
    fun testDocumentPath_relativizeIncludingRelativeParts() {
        /*
         * Unix path behavior:
         * "/a/b/..".relativize("/x/y/z") = "../x/y/z"
         */
        val pathA = DocumentPath(fileSystem, "tree", "a", "b", RELATIVE_PARENT_ID)
        val pathB = DocumentPath(fileSystem, "tree", "x", "y", "z")

        val expected = DocumentPath(
            fileSystem,
            "tree",
            RELATIVE_PARENT_ID, "x", "y", "z"
        )
        val relative = pathA.relativize(pathB) as DocumentPath
        assertDeepEquals(expected, relative)

        /*
         * Unix path behavior:
         * "/a/b/c".relativize("/x/../z") = "../../../z"
         */
        val pathA2 = DocumentPath(fileSystem, "tree", "a", "b", "c")
        val pathB2 = DocumentPath(fileSystem, "tree", "x", RELATIVE_PARENT_ID, "z")

        val expected2 = DocumentPath(
            fileSystem,
            "tree",
            RELATIVE_PARENT_ID, RELATIVE_PARENT_ID, RELATIVE_PARENT_ID, "z"
        )
        val relative2 = pathA2.relativize(pathB2) as DocumentPath
        assertDeepEquals(expected2, relative2)

        /*
         * Unix path behavior:
         * "/a".relativize("/x/../..") = "../.."
         */
        val pathA3 = DocumentPath(fileSystem, "tree", "a")
        val pathB3 = DocumentPath(fileSystem, "tree", "x", RELATIVE_PARENT_ID, RELATIVE_PARENT_ID)

        val expected3 = DocumentPath(
            fileSystem,
            "tree",
            RELATIVE_PARENT_ID, RELATIVE_PARENT_ID
        )
        val relative3 = pathA3.relativize(pathB3) as DocumentPath
        assertDeepEquals(expected3, relative3)
    }

    @Test
    fun testDocumentPath_relativizeInvalidRelativePath() {
        /*
         * Unix path behavior:
         * "/a/../..".relativize("/x/../z") -> throw IllegalArgumentException
         */
        val pathA = DocumentPath(fileSystem, "tree", "a", RELATIVE_PARENT_ID, RELATIVE_PARENT_ID)
        val pathB = DocumentPath(fileSystem, "tree", "x", "y", "z")

        try {
            val relativize = pathA.relativize(pathB)
            fail("Relativized path? $relativize")
        } catch (_: IllegalArgumentException) {
            // Pass -- These paths cannot be relativized.
        }
    }

    @Test
    fun testDocumentPath_resolveWithEmptyIsSame() {
        val path = DocumentPath(fileSystem, "tree", "grandparent", "parent", "child")
        val empty = DocumentPath(fileSystem, "tree")

        val resolved = path.resolve(empty)
        assertSame(path, resolved)
    }

    @Test
    fun testDocumentPath_resolveDifferentRootIsOther() {
        val pathA = DocumentPath(fileSystem, "root1", "cupcake", "abcd")
        val pathB = DocumentPath(fileSystem, "root2", "donut", "zyxw")

        val resolved = pathA.resolve(pathB)
        assertSame(pathB, resolved)
    }

    @Test
    fun testDocumentPath_resolveDifferentFileSystemsIsOther() {
        val providerA = URI("content://test.provider.a/tree/root")
        val fileSystemA = AndroidFileSystems.getFileSystem(providerA) as ContentFileSystem

        val providerB = URI("content://test.provider.b/tree/root")
        val fileSystemB = AndroidFileSystems.getFileSystem(providerB) as ContentFileSystem

        val pathA = DocumentPath(fileSystemA, "root", "parent", "child")
        val pathB = DocumentPath(fileSystemB, "root", "parent", "child")
        val resolved = pathA.resolve(pathB)
        assertSame(pathB, resolved)
    }

    @Test
    fun testDocumentPath_relativizeResolveEquals() {
        /*
         * Unix path behavior:
         * base = "/grandparent/parent"
         * extended = "/grandparent/parent/child"
         * rel = base.relativize(extended) = "child"
         * base.resolve(rel) = "/grandparent/parent/child"
         */
        val basePath = DocumentPath(fileSystem, "tree", "grandparent", "parent")
        val extended = DocumentPath(fileSystem, "tree", "grandparent", "parent", "child")

        val relative = basePath.relativize(extended)
        val resolve = basePath.resolve(relative)
        assertEquals(extended, resolve)
    }

    @Test
    fun testDocumentPath_normalizeWithRelativeParts() {
        /*
         * Unix path behavior:
         * "parent/../child".normalize() -> "child"
         */
        val path = DocumentPath(fileSystem, "tree", "parent", RELATIVE_PARENT_ID, "child")
        val expected = DocumentPath(fileSystem, "tree", "child")
        val normalized = path.normalize()
        assertEquals(expected, normalized)

        /*
         * Unix path behavior:
         * "parent/child/..".normalize() -> "parent"
         */
        val path2 = DocumentPath(fileSystem, "tree", "parent", "child", RELATIVE_PARENT_ID)
        val expected2 = DocumentPath(fileSystem, "tree", "parent")
        val normalized2 = path2.normalize()
        assertEquals(expected2, normalized2)
    }

    @Test
    fun testDocumentPath_normalizeOnlyRelativeSame() {
        /*
         * Unix path behavior:
         * "..".normalize() -> ".."
         */
        val path = DocumentPath(fileSystem, "tree", RELATIVE_PARENT_ID)
        val normalized = path.normalize()
        assertSame(path, normalized)

        val multiple = DocumentPath(fileSystem, "tree", RELATIVE_PARENT_ID, RELATIVE_PARENT_ID)
        val multiNormalized = multiple.normalize()
        assertSame(multiple, multiNormalized)
    }

    @Test
    fun testDocumentPath_normalizeNormalPathIsSelf() {
        val path = DocumentPath(fileSystem, "tree", "grandparent", "parent", "child")
        val normalized = path.normalize()
        assertSame(path, normalized)
    }

    @Test
    fun testDocumentPath_normalizeEmptyIsSame() {
        /*
         * Unix path behavior:
         * "".normalize() -> ""
         */
        val path = DocumentPath(fileSystem, "tree")
        val normalized = path.normalize()
        assertSame(path, normalized)
    }

    private fun assertDeepEquals(expected: DocumentPath, other: DocumentPath) {
        assertEquals(expected.fileSystem.authority, other.fileSystem.authority)
        assertEquals(expected.treeId, other.treeId)
        assertEquals(expected.path, other.path)
    }
}
