package com.google.modernstorage.filesystem.internal

import com.google.modernstorage.filesystem.ContentPath
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.DirectoryStream
import java.nio.file.DirectoryStream.Filter
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class TestContract : PlatformContract {
    override val scheme = "content"

    override fun isSupportedUri(uri: URI) = uri.scheme == scheme

    override fun prepareUri(incomingUri: URI) = incomingUri

    override fun openByteChannel(uri: URI, mode: String): SeekableByteChannel {
        TODO("Not yet implemented")
    }

    override fun newDirectoryStream(
        path: ContentPath,
        filter: Filter<in Path>?
    ): DirectoryStream<Path> {
        TODO("Not yet implemented")
    }

    override fun <A : BasicFileAttributes?> readAttributes(
        path: ContentPath,
        type: Class<A>?,
        vararg options: LinkOption?
    ): A {
        TODO("Not yet implemented")
    }
}