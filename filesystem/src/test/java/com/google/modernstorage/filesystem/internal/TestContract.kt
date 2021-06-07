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