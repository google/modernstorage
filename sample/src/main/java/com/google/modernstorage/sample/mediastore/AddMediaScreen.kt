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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.modernstorage.sample.Demos
import com.google.modernstorage.sample.HomeRoute
import com.google.modernstorage.sample.R
import com.google.modernstorage.sample.ui.shared.MediaPreviewCard

@ExperimentalFoundationApi
@Composable
fun AddMediaScreen(navController: NavController, viewModel: MediaStoreViewModel = viewModel()) {
    val addedFile by viewModel.addedFile.collectAsState()

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
                            onClick = { viewModel.addImage() }
                        ) {
                            Text(stringResource(R.string.demo_add_image_label))
                        }
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = { viewModel.addVideo() }
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
