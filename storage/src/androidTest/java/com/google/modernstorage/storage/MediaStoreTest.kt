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
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.source
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
class MediaStoreTest {
    private lateinit var appContext: Context
    private lateinit var fileSystem: AndroidFileSystem

    @get:Rule
    var readStoragePermission = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE)
    @get:Rule
    var writeStoragePermission = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        fileSystem = AndroidFileSystem(appContext)
    }

    private fun addFileFromAssets(extension: String, mimeType: String, collection: Uri, destination: File) {
        val filename = "added-${System.currentTimeMillis()}.$extension"
        val uri = fileSystem.createMediaStoreUri(
            filename,
            collection,
            destination.absolutePath
        )!!
        val path = uri.toOkioPath()

        fileSystem.write(path, false) {
            appContext.assets.open("sample.$extension").source().use { source ->
                writeAll(source)
            }
        }

        runBlocking {
            requireNotNull(fileSystem.scanUri(uri, mimeType))
        }

        val metadata = fileSystem.metadataOrNull(path)
        requireNotNull(metadata)

        Assert.assertEquals(filename, metadata.extra(MetadataExtras.DisplayName::class)!!.value)
        Assert.assertEquals(mimeType, metadata.extra(MetadataExtras.MimeType::class)!!.value)
        Assert.assertEquals("$destination/$filename", metadata.extra(MetadataExtras.FilePath::class)!!.value)

        verifyBytes(appContext.assets.open("sample.$extension"), path)
        appContext.contentResolver.delete(uri, null, null)
    }

    private fun verifyBytes(original: InputStream, target: Path) {
        original.use { inputStream ->
            val iterator = inputStream.readBytes().iterator()
            fileSystem.read(target) {
                do {
                    val a = iterator.next()
                    val b = this.readByte()
                    Assert.assertEquals(a, b)
                } while (iterator.hasNext() && !this.exhausted())
            }
        }
    }

    @Test
    fun addImage() {
        addFileFromAssets(
            "jpg",
            "image/jpeg",
            MediaStore.Images.Media.getContentUri("external"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        )
    }

    @Test
    fun addVideo() {
        addFileFromAssets(
            "mp4",
            "video/mp4",
            MediaStore.Video.Media.getContentUri("external"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        )
    }

    @Test
    fun addAudio() {
        addFileFromAssets(
            "wav",
            "audio/x-wav",
            MediaStore.Audio.Media.getContentUri("external"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        )
    }

    @Test
    fun addText() {
        addFileFromAssets(
            "txt",
            "text/plain",
            MediaStore.Files.getContentUri("external"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )
    }

    @Test
    fun addPdf() {
        addFileFromAssets(
            "pdf",
            "application/pdf",
            MediaStore.Files.getContentUri("external"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )
    }

    @Test
    fun addZip() {
        addFileFromAssets(
            "zip",
            "application/zip",
            MediaStore.Files.getContentUri("external"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )
    }

    @Test
    fun copyImageFromInternalStorage() {
        val internalFile = File(appContext.filesDir, "internal-${System.currentTimeMillis()}.jpg").also {
            appContext.assets.open("sample.jpg").copyTo(it.outputStream())
        }

        val uri = fileSystem.createMediaStoreUri(
            "added-${System.currentTimeMillis()}.jpg",
            MediaStore.Files.getContentUri("external"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        )!!
        val path = uri.toOkioPath()

        fileSystem.copy(internalFile.toOkioPath(), path)
        verifyBytes(internalFile.inputStream(), path)

        internalFile.delete()
        appContext.contentResolver.delete(uri, null, null)
    }

    @Test
    fun copyTextFromInternalStorage() {
        val internalFile = File(appContext.filesDir, "internal-${System.currentTimeMillis()}.txt").also {
            appContext.assets.open("sample.txt").copyTo(it.outputStream())
        }

        val uri = fileSystem.createMediaStoreUri(
            "added-${System.currentTimeMillis()}.txt",
            MediaStore.Files.getContentUri("external"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        )!!
        val path = uri.toOkioPath()

        fileSystem.copy(internalFile.toOkioPath(), path)
        verifyBytes(internalFile.inputStream(), path)

        internalFile.delete()
        appContext.contentResolver.delete(uri, null, null)
    }

    @Test
    fun copyPdfFromInternalStorage() {
        val internalFile = File(appContext.filesDir, "internal-${System.currentTimeMillis()}.pdf").also {
            appContext.assets.open("sample.pdf").copyTo(it.outputStream())
        }

        val uri = fileSystem.createMediaStoreUri(
            "added-${System.currentTimeMillis()}.pdf",
            MediaStore.Files.getContentUri("external"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        )!!
        val path = uri.toOkioPath()

        fileSystem.copy(internalFile.toOkioPath(), path)
        verifyBytes(internalFile.inputStream(), path)

        internalFile.delete()
        appContext.contentResolver.delete(uri, null, null)
    }
}
