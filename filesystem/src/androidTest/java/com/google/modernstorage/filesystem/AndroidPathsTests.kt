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
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.URI
import java.nio.file.FileSystems

/**
 * Unit tests for [AndroidPaths] that depend on Android specific methods.
 */
class AndroidPathsTests {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        AndroidFileSystems.initialize(context)
    }

    @Test
    fun constructPath_UriMatchesURI() {
        val mediaUriString = "content://media/external/files/media/1"

        val mediaPathFromUri = AndroidPaths.get(Uri.parse(mediaUriString))
        val mediaPathFromURI = AndroidPaths.get(URI(mediaUriString))
        assert(mediaPathFromUri.toUri() == mediaPathFromURI.toUri())
    }

    @Test
    fun constructPath_FileScheme() {
        val filePath = AndroidPaths.get(File(context.filesDir, "Test.txt").toURI())
        assert(filePath.fileSystem == FileSystems.getDefault())
    }
}
