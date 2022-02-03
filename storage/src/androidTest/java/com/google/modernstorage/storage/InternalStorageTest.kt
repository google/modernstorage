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
import okio.Path.Companion.toOkioPath
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

    private fun addFile(extension: String, mimeType: String, destination: File) {
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

        file.delete()
    }

    @Test
    fun addFilesInCacheFolder() {
        addFile("jpg", "image/jpeg", appContext.cacheDir)
        addFile("mp4", "video/mp4", appContext.cacheDir)
        addFile("wav", "audio/x-wav", appContext.cacheDir)
        addFile("txt", "text/plain", appContext.cacheDir)
        addFile("pdf", "application/pdf", appContext.cacheDir)
        addFile("zip", "application/zip", appContext.cacheDir)
    }

    @Test
    fun addFilesInDataFolder() {
        addFile("jpg", "image/jpeg", appContext.dataDir)
        addFile("mp4", "video/mp4", appContext.dataDir)
        addFile("wav", "audio/x-wav", appContext.dataDir)
        addFile("txt", "text/plain", appContext.dataDir)
        addFile("pdf", "application/pdf", appContext.dataDir)
        addFile("zip", "application/zip", appContext.dataDir)
    }
}
