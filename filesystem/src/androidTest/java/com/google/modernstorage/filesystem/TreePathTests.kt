/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.nio.file.Files

class TreePathTests {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val testUri =
        Uri.parse("content://${context.packageName}.documents/tree/files%2ftoot")

    @Before
    fun setup() {
        AndroidFileSystems.initialize(context)
    }

    @After
    fun teardown() {
    }

    @Test
    fun filesWalk_FlatDirectory() {
        val testPath = AndroidPaths.get(testUri)
        val directoryStream = Files.walk(testPath)
        directoryStream.forEach {
            Log.d("nicole", "Test: ${it.toUri()}")
        }
    }
}