package com.google.modernstorage.storage

import android.content.Context
import android.net.Uri
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source
import okio.source

class SharedFileSystem(context: Context): FileSystem() {
    private val contentResolver = context.contentResolver

    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        TODO("Not yet implemented")
    }

    override fun atomicMove(source: Path, target: Path) {
        TODO("Not yet implemented")
    }

    override fun canonicalize(path: Path): Path {
        TODO("Not yet implemented")
    }

    override fun createDirectory(dir: Path) {
        TODO("Not yet implemented")
    }

    override fun createSymlink(source: Path, target: Path) {
        TODO("Not yet implemented")
    }

    override fun delete(path: Path) {
        TODO("Not yet implemented")
    }

    override fun list(dir: Path): List<Path> {
        TODO("Not yet implemented")
    }

    override fun listOrNull(dir: Path): List<Path>? {
        TODO("Not yet implemented")
    }

    override fun metadataOrNull(path: Path): FileMetadata? {
        TODO("Not yet implemented")
    }

    override fun openReadOnly(file: Path): FileHandle {
        TODO("Not yet implemented")
    }

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        TODO("Not yet implemented")
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        TODO("Not yet implemented")
    }

    override fun source(file: Path): Source {
        val uri = file.toUri()
        val inputStream = contentResolver.openInputStream(uri)

        checkNotNull(inputStream) { "Couldn't open an InputStream for this Uri ($uri)" }

        return inputStream.source()
    }
}
