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
package com.google.modernstorage.sample

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.modernstorage.storage.SharedFileSystem
import com.google.modernstorage.storage.toPath
import okio.buffer

@ExperimentalMaterialApi
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val filesystem = SharedFileSystem(context)

    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        println("Selected Uri: $uri")

        uri?.let {
            filesystem.source(uri.toPath()).use { fileSource ->
                okio.blackholeSink().use { fileSink ->
                    fileSink.buffer().writeAll(fileSource)
                }
            }
        }
    }

    val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        println("Selected Uri: $uri")

        uri?.let {
            filesystem.source(uri.toPath()).use { fileSource ->
                okio.blackholeSink().use { fileSink ->
                    fileSink.buffer().writeAll(fileSource)
                }
            }
        }
    }

    Column {
        Button(onClick = { createDocument.launch("filename.txt") }) {
            Text("Open Document")
        }
        ListItem {
        }
    }
}
