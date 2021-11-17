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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@RunWith(AndroidJUnit4::class)
class StorageAccessFrameworkTest {
    private lateinit var device: UiDevice
    private lateinit var appContext: Context
    private lateinit var fileSystem: SharedFileSystem
    private lateinit var file: File

    @get:Rule
    var activityScenarioRule = activityScenarioRule<TestingActivity>()

    @Before
    fun setup() {
        device = UiDevice.getInstance(getInstrumentation())
        appContext = getInstrumentation().targetContext
        fileSystem = SharedFileSystem(appContext)
    }

    @After
    fun tearDown() {
        file.delete()
    }

    @Test
    fun readTextFile() {
        val content = "Hello".toByteArray(Charsets.UTF_8)
        file = addFileToDownloads("txt", "text/plain", content)

        activityScenarioRule.scenario.onActivity {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*")
            it.startActivityForResult(intent, 1)
        }

        runBlocking { delay(2000L) }

        val newTextFileEntry = device.findObject(UiSelector().textContains(file.name))
        newTextFileEntry.click()

        runBlocking { delay(2000L) }

        val resultIntent = activityScenarioRule.scenario.result
        assertEquals(resultIntent.resultCode, Activity.RESULT_OK)

        val receivedUri = resultIntent.resultData.data
        requireNotNull(receivedUri)

        val iterator = content.iterator()
        val source = fileSystem.source(receivedUri.toPath()).buffer()

        do {
            val a = iterator.next()
            val b = source.readByte()
            assertEquals(a, b)
        } while (iterator.hasNext() && !source.exhausted())

        source.close()
    }

    private fun generateFilename(extension: String) = "saf-${System.currentTimeMillis()}.$extension"

    private fun addFileToDownloads(extension: String, mimeType: String, bytes: ByteArray): File {
        val downloadFolder = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)
        val file = File(downloadFolder, generateFilename(extension))
        file.writeBytes(bytes)

        runBlocking {
            return@runBlocking scanFilePath(file.absolutePath, mimeType)
        }

        return file
    }

    /**
     * Every added file should be scanned to be seen immediately in MediaStore or the system file
     * navigator used by intents like ACTION_OPEN_DOCUMENT
     */
    private suspend fun scanFilePath(path: String, mimeType: String): Uri {
        return suspendCancellableCoroutine { continuation ->
            MediaScannerConnection.scanFile(
                appContext,
                arrayOf(path),
                arrayOf(mimeType)
            ) { _, scannedUri ->
                if (scannedUri != null) {
                    continuation.resume(scannedUri)
                } else {
                    continuation.resumeWithException(Exception("Scanning isn't supposed to fail"))
                }
            }
        }
    }
}
