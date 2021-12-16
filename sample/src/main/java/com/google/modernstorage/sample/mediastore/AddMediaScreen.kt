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
package com.google.modernstorage.sample.mediastore

import android.content.Context
import android.provider.MediaStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import okio.buffer
import okio.source

@ExperimentalFoundationApi
@Composable
fun AddMediaScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var addedFile by remember { mutableStateOf<FileDetails?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Demos.AddMedia.name)) },
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
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = { scope.launch { addedFile = addImage(context) } }
                        ) {
                            Text(stringResource(R.string.demo_add_image_label))
                        }
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = { scope.launch { addedFile = addVideo(context) } }
                        ) {
                            Text(stringResource(R.string.demo_add_video_label))
                        }
                    }

                    addedFile?.let {
                        item {
                            MediaPreviewCard(it)
                        }
                    }
                }
            }
        }
    )
}

private suspend fun addImage(context: Context): FileDetails? {
    val fileSystem = SharedFileSystem(context)

    val uri = fileSystem.createMediaStoreUri(
        filename = "added-${System.currentTimeMillis()}.jpg",
        collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    ) ?: return null
    val path = uri.toPath()

    fileSystem.sink(path).buffer().writeAll(context.assets.open("sample.jpg").source())
    fileSystem.scanUri(uri, "image/jpeg")

    val metadata = fileSystem.metadataOrNull(path) ?: return null
    return FileDetails(uri, path, metadata)
}

private suspend fun addVideo(context: Context): FileDetails? {
    val fileSystem = SharedFileSystem(context)

    val uri = fileSystem.createMediaStoreUri(
        filename = "added-${System.currentTimeMillis()}.mp4",
        collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    ) ?: return null
    val path = uri.toPath()

    fileSystem.sink(path).buffer().writeAll(context.assets.open("sample.mp4").source())
    fileSystem.scanUri(uri, "video/mp4")

    val metadata = fileSystem.metadataOrNull(path) ?: return null
    return FileDetails(uri, path, metadata)
}
