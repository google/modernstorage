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
package com.google.modernstorage.sample.saf

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.modernstorage.sample.Demos
import com.google.modernstorage.sample.HomeRoute
import com.google.modernstorage.sample.R
import com.google.modernstorage.sample.ui.shared.FileDetails
import com.google.modernstorage.sample.ui.shared.MediaPreviewCard
import com.google.modernstorage.storage.SharedFileSystem
import com.google.modernstorage.storage.toPath

const val GENERIC_MIMETYPE = "*/*"
const val PDF_MIMETYPE = "application/pdf"
const val ZIP_MIMETYPE = "application/zip"
const val IMAGE_MIMETYPE = "image/*"
const val VIDEO_MIMETYPE = "video/*"

@ExperimentalFoundationApi
@Composable
fun SelectDocumentFileScreen(navController: NavController) {
    val fileSystem = SharedFileSystem(LocalContext.current)
    var selectedFile by remember { mutableStateOf<FileDetails?>(null) }

    val selectFile =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { uri ->
                val path = uri.toPath()
                fileSystem.metadataOrNull(path)?.let { metadata ->
                    selectedFile = FileDetails(uri, path, metadata)
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Demos.SelectDocument.name)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack(HomeRoute, false) }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_label)
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            LazyColumn(Modifier.padding(paddingValues)) {
                item {
                    Button(
                        modifier = Modifier.padding(4.dp).fillMaxWidth(),
                        onClick = { selectFile.launch(arrayOf(GENERIC_MIMETYPE)) }
                    ) {
                        Text(stringResource(R.string.demo_select_any_document))
                    }
                }
                item {
                    Button(
                        modifier = Modifier.padding(4.dp).fillMaxWidth(),
                        onClick = { selectFile.launch(arrayOf(PDF_MIMETYPE)) }
                    ) {
                        Text(stringResource(R.string.demo_select_pdf_document))
                    }
                }
                item {
                    Button(
                        modifier = Modifier.padding(4.dp).fillMaxWidth(),
                        onClick = { selectFile.launch(arrayOf(ZIP_MIMETYPE)) }
                    ) {
                        Text(stringResource(R.string.demo_select_zip_document))
                    }
                }
                item {
                    Button(
                        modifier = Modifier.padding(4.dp).fillMaxWidth(),
                        onClick = { selectFile.launch(arrayOf(IMAGE_MIMETYPE, VIDEO_MIMETYPE)) }
                    ) {
                        Text(stringResource(R.string.demo_select_image_and_video_document))
                    }
                }

                selectedFile?.let {
                    item {
                        MediaPreviewCard(it)
                    }
                }
            }
        }
    )
}
