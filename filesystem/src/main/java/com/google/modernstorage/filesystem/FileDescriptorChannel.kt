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
package com.example.myapplication

import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.system.OsConstants.EBADF
import java.io.FileDescriptor
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonReadableChannelException
import java.nio.channels.NonWritableChannelException
import java.nio.channels.SeekableByteChannel

open class FileDescriptorChannel(
    private val fileDescriptor: FileDescriptor
) : SeekableByteChannel {

    private var isClosed = false

    override fun close() {
        if (!isClosed) {
            isClosed = true
            Os.close(fileDescriptor)
        }
    }

    override fun isOpen() = !isClosed

    override fun read(dst: ByteBuffer): Int {
        assertOpen()
        val bytes = try {
            Os.read(fileDescriptor, dst)
        } catch (errornoException: ErrnoException) {
            val errno = errornoException.errno
            when (errno) {
                EBADF -> throw NonReadableChannelException().initCause(errornoException)
                else -> throw IOException(errornoException)
            }
        }

        return if (bytes == 0) -1 else bytes
    }

    override fun write(src: ByteBuffer): Int {
        assertOpen()
        val bytes = try {
            Os.write(fileDescriptor, src)
        } catch (errornoException: ErrnoException) {
            val errno = errornoException.errno
            when (errno) {
                EBADF -> throw NonWritableChannelException().initCause(errornoException)
                else -> throw IOException(errornoException)
            }
        }
        return bytes
    }

    override fun position(): Long {
        return Os.lseek(fileDescriptor, 0, OsConstants.SEEK_CUR)
    }

    override fun position(newPosition: Long): SeekableByteChannel {
        Os.lseek(fileDescriptor, newPosition, OsConstants.SEEK_SET)
        return this
    }

    override fun size(): Long {
        val stats = Os.fstat(fileDescriptor)
        return stats.st_size
    }

    override fun truncate(size: Long): SeekableByteChannel {
        Os.ftruncate(fileDescriptor, size)
        return this
    }

    private fun assertOpen() {
        if (!isOpen) throw ClosedChannelException()
    }
}
