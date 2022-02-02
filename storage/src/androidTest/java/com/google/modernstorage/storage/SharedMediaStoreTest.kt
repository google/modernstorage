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
package com.google.modernstorage.storage

import android.Manifest
import android.content.Context
import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.source
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SharedMediaStoreTest {
    private lateinit var appContext: Context
    private lateinit var fileSystem: SharedFileSystem

    @get:Rule
    var readStoragePermission = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE)
    @get:Rule
    var writeStoragePermission = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        fileSystem = SharedFileSystem(appContext)
    }

    private fun addFile(extension: String, mimeType: String, destination: File) {
        val filename = "added-${System.currentTimeMillis()}.$extension"
        val uri = fileSystem.createMediaStoreUri(filename, destination.absolutePath)!!
        val path = uri.toPath()

        fileSystem.sink(path).buffer().writeAll(appContext.assets.open("sample.$extension").source())
        runBlocking {
            fileSystem.scanUri(uri, mimeType)
        }

        val metadata = fileSystem.metadataOrNull(path)
        requireNotNull(metadata)

        Assert.assertEquals(filename, metadata.extra(MetadataExtras.DisplayName::class)!!.value)
        Assert.assertEquals(mimeType, metadata.extra(MetadataExtras.MimeType::class)!!.value)

        val iterator = appContext.assets.open("sample.$extension").readBytes().iterator()
        val source = fileSystem.source(path).buffer()

        do {
            val a = iterator.next()
            val b = source.readByte()
            Assert.assertEquals(a, b)
        } while (iterator.hasNext() && !source.exhausted())

        source.close()
        appContext.contentResolver.delete(uri, null, null)
    }



    @Test
    fun addImage() {
        addFile(
            "jpg",
            "image/jpeg",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        )
    }

    @Test
    fun addVideo() {
        addFile(
            "mp4",
            "video/mp4",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        )
    }

    @Test
    fun addAudio() {
        addFile(
            "wav",
            "audio/x-wav",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        )
    }

    @Test
    fun addText() {
        addFile(
            "txt",
            "text/plain",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )
    }

    @Test
    fun addPdf() {
        addFile(
            "pdf",
            "application/pdf",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )
    }

    @Test
    fun addZip() {
        addFile(
            "zip",
            "application/zip",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )
    }
}
