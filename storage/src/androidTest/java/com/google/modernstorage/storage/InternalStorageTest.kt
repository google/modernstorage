/*
 * Copyright 2022 Google LLC
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
package com.google.modernstorage.storage

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.buffer
import okio.source
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class InternalStorageTest {
    private lateinit var appContext: Context
    private lateinit var fileSystem: AndroidFileSystem

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        fileSystem = AndroidFileSystem(appContext)
    }

    private fun addFile(extension: String, mimeType: String, destination: File): Path {
        val filename = "added-${System.currentTimeMillis()}.$extension"
        val file = File(destination, filename)
        val path = file.toOkioPath()

        fileSystem.write(path, false) {
            appContext.assets.open("sample.$extension").source().use { source ->
                writeAll(source)
            }
        }

        val metadata = fileSystem.metadataOrNull(path)
        requireNotNull(metadata)

        Assert.assertEquals(filename, metadata.extra(MetadataExtras.DisplayName::class)!!.value)
        Assert.assertEquals(mimeType, metadata.extra(MetadataExtras.MimeType::class)!!.value)

        appContext.assets.open("sample.$extension").use { inputStream ->
            val iterator = inputStream.readBytes().iterator()
            fileSystem.read(path) {
                do {
                    val a = iterator.next()
                    val b = this.readByte()
                    Assert.assertEquals(a, b)
                } while (iterator.hasNext() && !this.exhausted())
            }
        }

        return path
    }

    @Test
    fun addFilesInCacheFolder() {
        addFile("jpg", "image/jpeg", appContext.cacheDir).also {
            it.toFile().delete()
        }
        addFile("mp4", "video/mp4", appContext.cacheDir).also {
            it.toFile().delete()
        }
        addFile("wav", "audio/x-wav", appContext.cacheDir).also {
            it.toFile().delete()
        }
        addFile("txt", "text/plain", appContext.cacheDir).also {
            it.toFile().delete()
        }
        addFile("pdf", "application/pdf", appContext.cacheDir).also {
            it.toFile().delete()
        }
        addFile("zip", "application/zip", appContext.cacheDir).also {
            it.toFile().delete()
        }
    }

    @Test
    fun addFilesInDataFolder() {
        addFile("jpg", "image/jpeg", appContext.dataDir).also {
            it.toFile().delete()
        }
        addFile("mp4", "video/mp4", appContext.dataDir).also {
            it.toFile().delete()
        }
        addFile("wav", "audio/x-wav", appContext.dataDir).also {
            it.toFile().delete()
        }
        addFile("txt", "text/plain", appContext.dataDir).also {
            it.toFile().delete()
        }
        addFile("pdf", "application/pdf", appContext.dataDir).also {
            it.toFile().delete()
        }
        addFile("zip", "application/zip", appContext.dataDir).also {
            it.toFile().delete()
        }
    }

    @Test
    fun editFile() {
        val file = File(appContext.dataDir, "edit-${System.currentTimeMillis()}.txt").also {
            it.writeText("Hello World")
        }

        file.inputStream().use {
            Assert.assertEquals(String(it.readBytes()), "Hello World")
        }

        fileSystem.write(file.toOkioPath(), false) {
            writeUtf8("Storage on Android")
        }

        file.inputStream().use {
            Assert.assertEquals(String(it.readBytes()), "Storage on Android")
        }

        file.delete()
    }

    @Test
    fun appendToFile() {
        val file = File(appContext.dataDir, "edit-${System.currentTimeMillis()}.txt").also {
            it.writeText("Hello World")
        }

        file.inputStream().use {
            Assert.assertEquals(String(it.readBytes()), "Hello World")
        }

        fileSystem.appendingSink(file.toOkioPath()).use { sink ->
            sink.buffer().use {
                it.writeUtf8(" and Storage on Android")
            }
        }

        file.inputStream().use {
            Assert.assertEquals(String(it.readBytes()), "Hello World and Storage on Android")
        }

        file.delete()
    }

    @Test
    fun readFile() {
        val file = File(appContext.dataDir, "edit-${System.currentTimeMillis()}.txt").also {
            it.writeText("Hello World")
        }

        fileSystem.read(file.toOkioPath()) {
            Assert.assertEquals(readUtf8(), "Hello World")
        }

        file.inputStream().use {
            Assert.assertEquals(String(it.readBytes()), "Hello World")
        }

        file.delete()
    }

    @Test
    fun deleteFile() {
        val path = addFile("pdf", "application/pdf", appContext.dataDir)
        val actualFile = path.toFile()
        fileSystem.delete(path)

        Assert.assertFalse(actualFile.exists())
    }
}
