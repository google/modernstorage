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
 */
class DocumentPath private constructor(
    private val fileSystem: ContentFileSystem,
    val treeId: String?,
    elements: List<String> = emptyList()
) : Path {

    constructor(fileSystem: ContentFileSystem, treeId: String?, vararg elements: String) :
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
        val authority = fileSystem.authority
        val otherAuthority = other.fileSystem.authority
        return if (authority != otherAuthority) {
            authority.compareTo(otherAuthority)
        } else {
            "$treeId:$docId".compareTo("${other.treeId}:${other.docId}")
        }
    }

    override fun iterator(): MutableIterator<Path> {
        val components = mutableListOf<DocumentPath>()
        if (treeId != null) {
            components += DocumentPath(fileSystem, treeId)
        }
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

    override fun isAbsolute(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getRoot(): Path? {
        return if (treeId != null) {
            DocumentPath(fileSystem, treeId)
        } else {
            null
        }
    }

    override fun getFileName(): Path? {
        return docId?.let { DocumentPath(fileSystem, treeId, it) }
    }

    override fun getParent(): Path? {
        return if (path.size > 1) {
            DocumentPath(fileSystem, treeId, *path.subList(0, path.size - 1).toTypedArray())
        } else {
            null
        }
    }

    override fun getNameCount() = path.size

    override fun getName(index: Int): Path =
        DocumentPath(fileSystem, treeId, path[index])

    override fun subpath(beginIndex: Int, endIndex: Int): Path {
        TODO("Not yet implemented")
    }

    override fun startsWith(other: Path?): Boolean {
        TODO("Not yet implemented")
    }

    override fun startsWith(other: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun endsWith(other: Path?): Boolean {
        TODO("Not yet implemented")
    }

    override fun endsWith(other: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun normalize(): Path {
        TODO("Not yet implemented")
    }

    override fun resolve(other: Path?): Path {
        TODO("Not yet implemented")
    }

    override fun resolve(other: String?): Path {
        requireNotNull(other)
        return DocumentPath(fileSystem, treeId, path + other)
    }

    override fun resolveSibling(other: Path?): Path {
        TODO("Not yet implemented")
    }

    override fun resolveSibling(other: String?): Path {
        TODO("Not yet implemented")
    }

    override fun relativize(other: Path?): Path {
        TODO("Not yet implemented")
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

        // There may be elements at the "end" of the path that don't (yet) exist. That's fine
        // though, since we only need to resolve the _start_.
        val resolvedPath = fileSystem.provider().findDocumentPath(getName(0) as DocumentPath)

        // If the provider doesn't support `findDocumentPath`, return this path.
        if (resolvedPath.isEmpty()) return this

        // If the resolved path is a single element (that matches our first element), then this
        // path already is an absolute path, so return it, instead of building a new one.
        if (resolvedPath.size == 1 && resolvedPath[0] == path[0]) return this

        // Since we resolved the first document ID's path, the absolute path for this document is
        // the current path, with `resolvedPath` in place of `path[0]`.
        val elements = resolvedPath.toMutableList() + path.subList(1, path.size)
        return DocumentPath(fileSystem, treeId, elements)
    }

    override fun toRealPath(vararg options: LinkOption?): Path {
        TODO("Not yet implemented")
    }

    override fun toFile(): File {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "${fileSystem.authority}:$treeId//${docId.orEmpty()}"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is DocumentPath) return false
        return fileSystem.authority == other.fileSystem.authority &&
            treeId == other.treeId &&
            docId == other.docId
    }

    override fun hashCode() = Objects.hash(fileSystem.authority, treeId, docId)
}
