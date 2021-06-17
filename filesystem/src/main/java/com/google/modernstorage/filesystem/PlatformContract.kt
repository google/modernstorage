/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.modernstorage.filesystem

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