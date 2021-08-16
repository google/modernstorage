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

import android.content.Context
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.parseMode
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.FileDescriptorChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.channels.Channels
import java.nio.channels.NonReadableChannelException
import java.nio.channels.NonWritableChannelException
import java.nio.file.Files

/**
 * Basic tests for [FileDescriptorChannel] class.
 */
class FileDescriptorChannelTests {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun testRead() {
        val testFile = File(context.filesDir, "test.txt").apply {
            deleteOnExit()
        }
        if (!testFile.exists()) Files.createFile(testFile.toPath())

        val content = listOf(
            "This is some test content",
            "That spans more than one line."
        )
        BufferedWriter(FileWriter(testFile)).use { writer ->
            content.forEach { line ->
                writer.write(line)
                writer.newLine()
            }
        }

        val pfd = ParcelFileDescriptor.open(testFile, parseMode("r"))
        val channel = FileDescriptorChannel(pfd.fileDescriptor)
        val stream = Channels.newInputStream(channel)
        BufferedReader(InputStreamReader(stream)).use { reader ->
            var index = 0
            reader.forEachLine { line ->
                assertEquals(content[index], line)
                index += 1
            }
        }
    }

    @Test
    fun testWrite() {
        val testFile = File(context.filesDir, "test.txt").apply {
            deleteOnExit()
        }
        if (!testFile.exists()) Files.createFile(testFile.toPath())

        val content = listOf(
            "This is some test content",
            "That spans more than one line."
        )

        val pfd = ParcelFileDescriptor.open(testFile, parseMode("wt"))
        val channel = FileDescriptorChannel(pfd.fileDescriptor)
        val stream = Channels.newOutputStream(channel)
        BufferedWriter(OutputStreamWriter(stream)).use { writer ->
            content.forEach { line ->
                writer.write(line)
                writer.newLine()
            }
        }

        BufferedReader(FileReader(testFile)).use { reader ->
            var index = 0
            reader.forEachLine { line ->
                assertEquals(content[index], line)
                index += 1
            }
        }
    }

    @Test
    fun testRead_notOpenForRead() {
        val testFile = File(context.filesDir, "test.txt").apply {
            deleteOnExit()
        }
        if (!testFile.exists()) Files.createFile(testFile.toPath())

        val pfd = ParcelFileDescriptor.open(testFile, parseMode("wt"))
        val channel = FileDescriptorChannel(pfd.fileDescriptor)
        try {
            val stream = Channels.newInputStream(channel)
            BufferedReader(InputStreamReader(stream)).use { reader ->
                reader.forEachLine { line ->
                    fail("Read from FileDescriptor open for writing")
                }
            }
        } catch (_: NonReadableChannelException) {
            // Pass
        }
    }

    @Test
    fun testWrite_notOpenForWrite() {
        val testFile = File(context.filesDir, "test.txt").apply {
            deleteOnExit()
        }
        if (!testFile.exists()) Files.createFile(testFile.toPath())

        val pfd = ParcelFileDescriptor.open(testFile, parseMode("r"))
        val channel = FileDescriptorChannel(pfd.fileDescriptor)
        try {
            val stream = Channels.newOutputStream(channel)
            BufferedWriter(OutputStreamWriter(stream)).use { writer ->
                writer.write("This is a test")
            }
            fail("Wrote to FileDescriptor only open for reading")
        } catch (_: NonWritableChannelException) {
            // Pass
        }
    }
}
