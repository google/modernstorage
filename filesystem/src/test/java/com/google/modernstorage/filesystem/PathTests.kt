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

import com.google.modernstorage.filesystem.internal.TestContract
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.URI
import java.nio.file.Paths
import java.util.Locale

/**
 * Unit tests for [AndroidPaths], [ContentPath], and [ExternalStoragePath].
 */
class PathTests {

    @Before
    fun setup() {
        AndroidFileSystems.initialize(TestContract())
    }

    @Test
    fun constructPath_CorrectContentPath() {
        val mediaUriString = "content://media/external/files/media/1"

        val mediaPath = AndroidPaths.get(URI(mediaUriString))
        assert(mediaPath is ContentPath)
        assert(mediaPath !is ExternalStoragePath)
    }

    @Test
    fun constructPath_CorrectExternalStoragePath() {
        val externalUriString =
            "content://com.android.externalstorage.documents/tree/primary%3ATest/document/primary%3ATest"

        val storagePath = AndroidPaths.get(URI(externalUriString))
        assert(storagePath is ExternalStoragePath)
    }

    @Test
    fun testAndroidPaths_Fallback() {
        val fallbackUri = File("/tmp/some/Path.txt").toURI()

        val pathFromAndroidPaths = AndroidPaths.get(fallbackUri)
        val pathFromPaths = Paths.get(fallbackUri)
        assert(pathFromAndroidPaths == pathFromPaths)
    }

    @Test
    fun testContentPath_CompareToSame() {
        val uriTitleCase =
            "content://com.android.externalstorage.documents/tree/primary%3ATest/document/primary%3ATest"
        val uriLowerCase = uriTitleCase.toLowerCase(Locale.ROOT)

        val titlePath = AndroidPaths.get(URI(uriTitleCase))
        val lowerPath = AndroidPaths.get(URI(uriLowerCase))
        Assert.assertTrue(titlePath.compareTo(lowerPath) == 0)
    }

    @Test
    fun testContentPath_CompareToOther() {
        val primaryUri =
            "content://com.android.externalstorage.documents/tree/primary%3ATest/document/primary%3ATest"
        val externalUri =
            "content://com.android.externalstorage.documents/tree/1000-200%3ATest/document/primary%3ATest"

        val primaryPath = AndroidPaths.get(URI(primaryUri))
        val externalPath = AndroidPaths.get(URI(externalUri))
        Assert.assertFalse(primaryPath.compareTo(externalPath) == 0)
    }

    @Test
    fun pathGetParent_validParent() {
        AndroidFileSystems.initialize(
            TestContract(
                getDocumentIdImpl = { uri ->
                    uri.toString().substringAfter("/document/")
                }
            )
        )
        val childUri =
            URI("content://com.android.externalstorage.documents/tree/primary/document/primary%3ADownload%2Ftest-3.txt")
        val childPath = AndroidPaths.get(childUri)

        val expectedParent = AndroidPaths.get(
            URI("content://com.android.externalstorage.documents/tree/primary/document/primary%3ADownload%2F")
        )
        Assert.assertEquals(expectedParent, childPath.parent)
    }
}
