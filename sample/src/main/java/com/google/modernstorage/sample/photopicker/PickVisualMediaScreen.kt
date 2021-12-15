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
package com.google.modernstorage.sample.photopicker

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
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
import com.google.modernstorage.photopicker.PhotoPicker
import com.google.modernstorage.sample.Demos
import com.google.modernstorage.sample.HomeRoute
import com.google.modernstorage.sample.R
import com.google.modernstorage.sample.ui.shared.MediaPreviewCard
import com.google.modernstorage.storage.SharedFileSystem
import com.google.modernstorage.storage.toPath
import okio.FileMetadata

private const val IMAGE_MIMETYPE = "image/*"
private const val VIDEO_MIMETYPE = "video/*"

@SuppressLint("UnsafeOptInUsageError")
@ExperimentalFoundationApi
@Composable
fun PickVisualMediaScreen(navController: NavController) {
    val fileSystem = SharedFileSystem(LocalContext.current)
    var selectedFiles by remember { mutableStateOf<List<Pair<Uri, FileMetadata?>>>(emptyList()) }

    val selectFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { selectedFiles = listOf(it to fileSystem.metadataOrNull(it.toPath())) }
    }

    val selectMultipleFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        selectedFiles = uris.map { it to fileSystem.metadataOrNull(it.toPath()) }
    }

    val photoPicker = rememberLauncherForActivityResult(PhotoPicker()) { uris ->
        selectedFiles = uris.map { it to fileSystem.metadataOrNull(it.toPath()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Demos.SelectDocumentFile.name)) },
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
            Column(Modifier.padding(paddingValues)) {
                LazyVerticalGrid(cells = GridCells.Fixed(1)) {
                    item {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = stringResource(R.string.demo_system_file_picker),
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = { selectFile.launch(arrayOf(IMAGE_MIMETYPE)) }
                        ) {
                            Text(stringResource(R.string.demo_pick_image))
                        }
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = { selectFile.launch(arrayOf(VIDEO_MIMETYPE)) }
                        ) {
                            Text(stringResource(R.string.demo_pick_video))
                        }
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = { selectFile.launch(arrayOf(IMAGE_MIMETYPE, VIDEO_MIMETYPE)) }
                        ) {
                            Text(stringResource(R.string.demo_pick_image_and_video))
                        }
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = { selectMultipleFiles.launch(arrayOf(IMAGE_MIMETYPE, VIDEO_MIMETYPE)) }
                        ) {
                            Text(stringResource(R.string.demo_pick_multiple_images_and_video))
                        }
                    }

                    item {
                        Divider(Modifier.padding(vertical = 10.dp))
                    }

                    item {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = stringResource(R.string.demo_photo_picker),
                            style = MaterialTheme.typography.subtitle1
                        )
                    }

                    item {
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = { photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_ONLY, 1)) }
                        ) {
                            Text(stringResource(R.string.demo_pick_image))
                        }
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = { photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.VIDEO_ONLY, 1)) }
                        ) {
                            Text(stringResource(R.string.demo_pick_video))
                        }
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = { photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_AND_VIDEO, 1)) }
                        ) {
                            Text(stringResource(R.string.demo_pick_image_and_video))
                        }
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = { photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_AND_VIDEO, 10)) }
                        ) {
                            Text(stringResource(R.string.demo_pick_multiple_images_video_photo_picker))
                        }
                    }

                    items(selectedFiles) {
                        it.second?.let { metadata -> MediaPreviewCard(it.first.toPath(), metadata) }
                    }
                }
            }
        }
    )
}