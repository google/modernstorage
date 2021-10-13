package com.google.modernstorage.storage

import android.content.Context
import okio.ExperimentalFileSystem
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source

@ExperimentalFileSystem
class AndroidFileSystem(context: Context): FileSystem() {
    private val contentResolver = context.contentResolver

    override fun appendingSink(file: Path): Sink {
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

    override fun delete(path: Path) {
        TODO("Not yet implemented")
    }

    override fun list(dir: Path): List<Path> {
        TODO("Not yet implemented")
    }

    override fun metadataOrNull(path: Path): FileMetadata? {
        TODO("Not yet implemented")
    }

    override fun openReadOnly(file: Path): FileHandle {
        TODO("Not yet implemented")
    }

    override fun openReadWrite(file: Path): FileHandle {
        TODO("Not yet implemented")
    }

    override fun sink(file: Path): Sink {
        TODO("Not yet implemented")
    }

    override fun source(file: Path): Source {
        TODO("Not yet implemented")
    }
}
