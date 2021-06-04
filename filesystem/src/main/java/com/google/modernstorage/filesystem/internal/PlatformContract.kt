package com.google.modernstorage.filesystem.internal

import com.google.modernstorage.filesystem.ContentFileSystemProvider
import com.google.modernstorage.filesystem.ContentPath
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.DirectoryStream
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

/**
 * Contract interface for platform specific implementations of methods required by
 * [ContentFileSystemProvider].
 *
 * This interface is nominally implemented by [AndroidContentContract].
 */
interface PlatformContract {
    val scheme: String

    fun isSupportedUri(uri: URI): Boolean

    fun prepareUri(incomingUri: URI): URI

    fun openByteChannel(uri: URI, mode: String): SeekableByteChannel

    fun newDirectoryStream(
        path: ContentPath,
        filter: DirectoryStream.Filter<in Path>?
    ): DirectoryStream<Path>

    fun <A : BasicFileAttributes?> readAttributes(
        path: ContentPath,
        type: Class<A>?,
        vararg options: LinkOption?
    ): A
}