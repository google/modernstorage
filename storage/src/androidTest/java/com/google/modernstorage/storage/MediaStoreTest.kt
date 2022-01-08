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
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaStoreTest {
    private lateinit var appContext: Context
    private lateinit var fileSystem: SharedFileSystem
    private lateinit var uri: Uri

    @get:Rule
    var readStoragePermission = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE)
    @get:Rule
    var writeStoragePermission = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        fileSystem = SharedFileSystem(appContext)
    }

    @After
    fun tearDown() {
        appContext.contentResolver.delete(uri, null, null)
    }

    @Test
    fun addImageFile() {
        val filename = "added-${System.currentTimeMillis()}.jpg"
        val content = appContext.assets.open("sample.jpg")
        val mimeType = "image/jpeg"

        uri = fileSystem.createMediaStoreUri(
            filename = filename,
            directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        )!!
        val path = uri.toPath()

        fileSystem.sink(path).buffer().writeAll(content.source())
        runBlocking {
            fileSystem.scanUri(uri, mimeType)
        }

        val metadata = fileSystem.metadataOrNull(path)
        requireNotNull(metadata)

        Assert.assertEquals(filename, metadata.extra(MetadataExtras.DisplayName::class)!!.value)
        Assert.assertEquals(mimeType, metadata.extra(MetadataExtras.MimeType::class)!!.value)

        val iterator = appContext.assets.open("sample.jpg").readBytes().iterator()
        val source = fileSystem.source(path).buffer()

        do {
            val a = iterator.next()
            val b = source.readByte()
            Assert.assertEquals(a, b)
        } while (iterator.hasNext() && !source.exhausted())

        source.close()
    }

    @Test
    fun addVideoFile() {
        val filename = "added-${System.currentTimeMillis()}.mp4"
        val content = appContext.assets.open("sample.mp4")
        val mimeType = "video/mp4"

        uri = fileSystem.createMediaStoreUri(
            filename = filename,
            directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath
        )!!
        val path = uri.toPath()

        fileSystem.sink(path).buffer().writeAll(content.source())
        runBlocking {
            fileSystem.scanUri(uri, mimeType)
        }

        val metadata = fileSystem.metadataOrNull(path)
        requireNotNull(metadata)

        Assert.assertEquals(filename, metadata.extra(MetadataExtras.DisplayName::class)!!.value)
        Assert.assertEquals(mimeType, metadata.extra(MetadataExtras.MimeType::class)!!.value)

        val iterator = appContext.assets.open("sample.mp4").readBytes().iterator()
        val source = fileSystem.source(path).buffer()

        do {
            val a = iterator.next()
            val b = source.readByte()
            Assert.assertEquals(a, b)
        } while (iterator.hasNext() && !source.exhausted())

        source.close()
    }

    @Test
    fun addAudioFile() {
        val filename = "added-${System.currentTimeMillis()}.wav"
        val content = appContext.assets.open("sample.wav")
        val mimeType = "audio/x-wav"

        uri = fileSystem.createMediaStoreUri(
            filename = filename,
            directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath
        )!!
        val path = uri.toPath()

        fileSystem.sink(path).buffer().writeAll(content.source())
        runBlocking {
            fileSystem.scanUri(uri, mimeType)
        }

        val metadata = fileSystem.metadataOrNull(path)
        requireNotNull(metadata)

        Assert.assertEquals(filename, metadata.extra(MetadataExtras.DisplayName::class)!!.value)
        Assert.assertEquals(mimeType, metadata.extra(MetadataExtras.MimeType::class)!!.value)

        val iterator = appContext.assets.open("sample.wav").readBytes().iterator()
        val source = fileSystem.source(path).buffer()

        do {
            val a = iterator.next()
            val b = source.readByte()
            Assert.assertEquals(a, b)
        } while (iterator.hasNext() && !source.exhausted())

        source.close()
    }
}
