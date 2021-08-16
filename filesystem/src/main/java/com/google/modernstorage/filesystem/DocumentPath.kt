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

import androidx.annotation.VisibleForTesting
import java.io.File
import java.net.URI
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.util.Objects

/**
 * Path representing a Document backed by an [android.provider.DocumentsProvider].
 *
 * Instances of this class are not directly created, but are returned from [AndroidPaths.get] when
 * the provided [android.net.Uri] is backed by a `DocumentsProvider`.
 */
class DocumentPath private constructor(
    private val fileSystem: ContentFileSystem,
    val treeId: String?,
    elements: List<String> = emptyList(),
    private val isAbsolutePath: Boolean = false
) : Path {

    /**
     * Constructs a [DocumentPath] for a specific [ContentFileSystem], given a tree document ID and
     * list of document IDs that represent the 'path' from eldest ancestor to child.
     * This method works very similarly to [java.nio.file.Paths.get].
     */
    internal constructor(fileSystem: ContentFileSystem, treeId: String?, vararg elements: String) :
        this(fileSystem, treeId, elements.toList())

    @VisibleForTesting
    val path = elements.toMutableList()
    val docId get() = if (path.isNotEmpty()) path.last() else null

    init {
        if (treeId == null && elements.isEmpty()) {
            throw IllegalArgumentException("Path must have at least a tree ID or document ID")
        }
    }

    override fun compareTo(other: Path?): Int {
        if (other !is DocumentPath) {
            throw IllegalArgumentException("Cannot compare non-DocumentPaths")
        }
        return toString().compareTo(other.toString())
    }

    override fun iterator(): MutableIterator<Path> {
        val components = mutableListOf<DocumentPath>()
        path.forEach { element ->
            components += DocumentPath(fileSystem, treeId, element)
        }
        return components.listIterator()
    }

    override fun register(
        watcher: WatchService?,
        events: Array<out WatchEvent.Kind<*>>?,
        vararg modifiers: WatchEvent.Modifier?
    ): WatchKey {
        TODO("Not yet implemented")
    }

    override fun register(watcher: WatchService?, vararg events: WatchEvent.Kind<*>?): WatchKey {
        TODO("Not yet implemented")
    }

    override fun getFileSystem() = fileSystem

    override fun isAbsolute() = isAbsolutePath

    override fun getRoot(): Path? {
        return if (treeId != null) {
            DocumentPath(fileSystem, treeId)
        } else {
            null
        }
    }

    override fun getFileName(): Path? {
        return if (path.isNotEmpty()) getName(path.size - 1) else null
    }

    override fun getParent(): Path? {
        return if (path.size > 1) {
            subpath(0, path.size - 1)
        } else {
            null
        }
    }

    override fun getNameCount() = path.size

    override fun getName(index: Int): Path =
        DocumentPath(fileSystem, treeId, listOf(path[index]), index == 0 && isAbsolutePath)

    override fun subpath(beginIndex: Int, endIndex: Int): Path {
        if (beginIndex >= 0 && endIndex <= path.size && beginIndex < endIndex) {
            // Will the new path be an absolute path?
            val absolute = (beginIndex == 0 && isAbsolutePath)
            return DocumentPath(fileSystem, treeId, path.subList(beginIndex, endIndex), absolute)
        } else {
            throw IllegalArgumentException("Invalid indexes: $beginIndex..$endIndex")
        }
    }

    override fun startsWith(other: Path?): Boolean {
        /*
         * Get rid of a whole bunch of basic checks at the start.
         * For a path to "startWith" another, it must:
         * - Be a DocumentPath
         * - Reference the same provider + tree
         * - Not be an "empty" path
         */
        if (other !is DocumentPath) return false
        if (other.fileSystem.authority != fileSystem.authority) return false
        if (other.treeId != treeId) return false
        if (other.path.isEmpty()) return false

        // If other is _longer_ than this path, it also can't "start" with it
        if (other.path.size > path.size) return false

        // Check from the start and ensure each document id matches
        other.path.forEachIndexed { index, part ->
            if (path[index] != part) return false
        }
        // If we get here, the path starts with that one!
        return true
    }

    override fun startsWith(other: String): Boolean {
        return startsWith(DocumentPath(fileSystem, treeId, other))
    }

    override fun endsWith(other: Path): Boolean {
        /*
         * Similar to `startsWith`, get rid of a whole bunch of basic checks at the start.
         * For a path to "endsWith" another, it must:
         * - Be a DocumentPath
         * - Reference the same provider + tree
         * - Not be an "empty" path
         */
        if (other !is DocumentPath) return false
        if (other.fileSystem.authority != fileSystem.authority) return false
        if (other.treeId != treeId) return false
        if (other.path.isEmpty()) return false

        // If other is _longer_ than this path, it also can't "end" with it
        if (other.path.size > path.size) return false

        // Check from the end of each list of document ids
        // Since we know that the list of document ids in other is _at most_ the same as the
        // number of ids in this path, we don't need to do additional range checks here.
        val lastIndex = path.size - 1
        val otherLast = other.path.size - 1
        for (index in 0 until other.path.size) {
            // `index` is how many elements from the last one to check
            if (path[lastIndex - index] != other.path[otherLast - index]) return false
        }
        // If we get here, the path ends with that one!
        return true
    }

    override fun endsWith(other: String): Boolean {
        return endsWith(DocumentPath(fileSystem, treeId, other))
    }

    override fun normalize(): Path {
        if (path.isEmpty()) return this

        val newParts = mutableListOf<String>()

        // We only need to construct a new path if something in this path changed. This only
        // happens if there's a '..' that can be removed.
        var pathUpdated = false
        path.forEach { element ->
            if (element != RELATIVE_PARENT_ID) {
                newParts.add(element)
            } else if (newParts.isNotEmpty() && newParts.last() != RELATIVE_PARENT_ID) {
                newParts.removeLast()
                pathUpdated = true
            } else {
                newParts.add(RELATIVE_PARENT_ID)
            }
        }
        return if (pathUpdated) DocumentPath(fileSystem, treeId, newParts) else this
    }

    override fun resolve(other: Path): Path {
        if (other !is DocumentPath) {
            throw IllegalArgumentException("Cannot resolve against non-DocumentPaths")
        }

        if (other.fileSystem.authority != fileSystem.authority) return other
        if (other.treeId == treeId && other.path.isEmpty()) return this
        if (other.treeId != null && treeId != other.treeId) return other

        val newPath = path.toMutableList()
        newPath.addAll(other.path)
        return DocumentPath(fileSystem, treeId, newPath, isAbsolutePath)
    }

    override fun resolve(other: String): Path {
        return resolve(DocumentPath(fileSystem, treeId, other))
    }

    override fun resolveSibling(other: Path): Path {
        if (other !is DocumentPath) {
            throw IllegalArgumentException("Cannot resolve against non-DocumentPaths")
        }

        if (fileSystem.authority != other.fileSystem.authority) return other
        if (treeId != other.treeId) return other
        return parent?.resolve(other) ?: other
    }

    override fun resolveSibling(other: String): Path {
        return resolveSibling(DocumentPath(fileSystem, treeId, other))
    }

    override fun relativize(other: Path): Path {
        if (other !is DocumentPath) {
            throw IllegalArgumentException("Cannot relativize against non-DocumentPaths")
        }
        if (other.fileSystem.authority != fileSystem.authority) {
            throw IllegalArgumentException("Cannot relativize across providers")
        }
        if (other.treeId != treeId) {
            throw IllegalArgumentException("Cannot relativize across trees")
        }

        var index = 0
        val newPath = mutableListOf<String>()

        // The paths may not be the same size, so we'll stop checking when we get to the end of
        // the shorter of the two. i.e.: Resolving "/a/b/c" and "/a/x/y" -> "b/c" and "x/y"
        val endIndex = if (path.size <= other.path.size) path.size else other.path.size

        // Skip over path elements that are the same
        while (index < endIndex && path[index] == other.path[index]) {
            ++index
        }

        // If there are more parts of "this" path left, then they need to be replaced with ".."-y
        // bits. i.e.: if we have "b/c" left, this needs to become "../.."
        for (dots in index until path.size) {
            if (path[dots] != RELATIVE_PARENT_ID) {
                newPath.add(RELATIVE_PARENT_ID)
            } else {
                // Special case for when there's a ".." in the base path. In this case, if
                // there's at least one element, then we don't add this "..", and we remove the
                // existing element (to resolve the ".." to it's parent).
                // If there _isn't_ a path element left to remove, we throw, which matches what
                // happens with UnixPath.
                if (newPath.isEmpty()) {
                    throw IllegalArgumentException("Cannot create relative path from $this -> $other")
                }
                newPath.removeLast()
            }
        }

        // Then, if there are parts of the "other" path left, they need to be copied into the new
        // path. i.e.: the "x/y" needs to be appended to the "../.." above.
        for (remain in index until other.path.size) {
            if (other.path[remain] != RELATIVE_PARENT_ID) {
                newPath.add(other.path[remain])
            } else {
                // Similar to the above, but since this is the path we're building a relative path
                // to, it's okay if it contains more relative components.
                if (newPath.isNotEmpty() && newPath.last() != RELATIVE_PARENT_ID) {
                    newPath.removeLast()
                } else {
                    newPath.add(RELATIVE_PARENT_ID)
                }
            }
        }

        // Finally, construct a new path ("../../x/y" in our example)
        return DocumentPath(fileSystem, treeId, newPath)
    }

    override fun toUri(): URI {
        // Since `docId` is a getter (and `path` is mutable), we need to capture the value of
        // `docId` at the start.
        val theDocId = docId
        return if (treeId != null) {
            val tree = fileSystem.provider().buildTreeDocumentUri(fileSystem.authority, treeId)
            if (theDocId != null) {
                fileSystem.provider().buildDocumentUriUsingTree(tree, theDocId)
            } else {
                tree
            }
        } else if (theDocId != null) {
            fileSystem.provider().buildDocumentUri(fileSystem.authority, theDocId)
        } else {
            throw IllegalStateException("Path without tree or document ID: $this")
        }
    }

    /**
     * Returns a [Path] object representing the absolute path of this path.
     *
     * If this path does not refer to a "tree", or is a root itself (a Path that only
     * includes a `treeId`) or is already an absolute path, this method returns the same
     * path.
     *
     * If the [android.provider.DocumentsProvider] that is associated with this path does not
     * support [android.provider.DocumentsContract.findDocumentPath], then this method will also
     * return itself.
     *
     * Paths are resolved as such:
     * Input path: {treeId="root", path=["b", "c", "d"]}
     *
     * In this case, since the path is a tree (has a `treeId`) and has at least one element
     * in the `path`, this method will call `DocumentsContract.findDocumentPath` with a tree
     * Uri constructed like this:
     *
     * ```
     * val baseTreeUri = DocumentsContract.buildTreeDocumentUri(authority, "root")
     * val resolveUri = DocumentsContract.buildDocumentUriUsingTree(baseTreeUri, "b")
     * ```
     *
     * `resolveUri` is then passed to `DocumentsContract.findDocumentPath`, which might return:
     * `["a", "b"]`.
     *
     * This method compares the document ID it based the request on ("b") to the first element of
     * the returned list ("a") and finds that there are missing elements. It then builds a new
     * `absolute` path by removing the first element of it's own path ("b"), since that document
     * ID is necessarily returned by `findDocumentPath`. It then builds the new path with the
     * same tree ID, and a `path` that is the list of document IDs returned by `findDocumentPath`
     * + the remaining document IDs in this path. In our example that would be:
     * `{treeId="root", path=["a", "b"] + "["c", "d"]}`.
     */
    override fun toAbsolutePath(): Path {
        // If it's not a tree, there's no way to resolve an "absolute" path via the
        // DocumentProvider so just return this path.
        if (treeId == null) return this

        // If there aren't any documents in the path, then just return this path
        if (path.isEmpty()) return this

        // If this path is already absolute, then return this one
        if (isAbsolute) return this

        // There may be elements at the "end" of the path that don't (yet) exist. That's fine
        // though, since we only need to resolve the _start_.
        val resolvedPath = fileSystem.provider().findDocumentPath(getName(0) as DocumentPath)

        // If the provider doesn't support `findDocumentPath`, then it's impossible to get an
        // absolute path.
        if (resolvedPath.isEmpty()) return this

        // Since we resolved the first document ID's path, the absolute path for this document is
        // the current path, with `resolvedPath` in place of `path[0]`.
        val elements = resolvedPath.toMutableList() + path.subList(1, path.size)
        return DocumentPath(fileSystem, treeId, elements, isAbsolutePath = true)
    }

    override fun toRealPath(vararg options: LinkOption?): Path {
        TODO("Not yet implemented")
    }

    override fun toFile(): File {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        val pathString = path.joinToString(
            separator = fileSystem.separator,
            prefix = if (isAbsolutePath) fileSystem.separator else ""
        )
        val treeString = if (treeId == null) "" else "@$treeId"
        return "//${fileSystem.authority}$treeString:$pathString"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is DocumentPath) return false
        return fileSystem.authority == other.fileSystem.authority &&
            treeId == other.treeId && path == other.path
    }

    override fun hashCode() = Objects.hash(fileSystem.authority, treeId, path)

    internal fun updateDocId(newDocId: String) {
        synchronized(path) {
            path[path.size - 1] = newDocId
        }
    }
}

/**
 * Implementation specific version of ".." for building relative paths that go "up" a
 * document tree. In a Unix file system, the ".." is not a valid file name, but this ID
 * _could_ be used, it's just constructed to be very unlikely.
 *
 * The initial version is a ".." with a Zero Width Joiner between them.
 */
internal const val RELATIVE_PARENT_ID = ".\u200D."
