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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.BottomDrawer
import androidx.compose.material.BottomDrawerValue
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun AppScreen(viewModel: AppViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
    var selectedDemo by remember { mutableStateOf<Demo?>(null) }

    suspend fun onDemoSelect(demo: Demo) {
        selectedDemo = demo
        drawerState.open()
    }

    BottomDrawer(
        gesturesEnabled = !drawerState.isClosed,
        drawerState = drawerState,
        drawerContent = {
            Column(Modifier.fillMaxSize()) {
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    onClick = { scope.launch { drawerState.close() } },
                    content = { Text("Close Drawer") }
                )
                Divider()
                selectedDemo?.let { Text(it.name) }
                when (selectedDemo) {
                    Demo.ADD_MEDIA -> AddMediaDemo(viewModel)
                    Demo.ADD_DOWNLOAD_FILE -> AddDownloadFileDemo(viewModel)
                }
            }
        },
        content = {
            DemoList { demo -> scope.launch { onDemoSelect(demo) } }
        }
    )
}

enum class Demo {
    ADD_MEDIA, ADD_DOWNLOAD_FILE
}

@ExperimentalMaterialApi
@Composable
fun DemoList(onClick: (Demo) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        for (demo in Demo.values()) {
            ListItem(
                modifier = Modifier.clickable { onClick(demo) },
                text = { Text(demo.name) },
            )
            Divider()
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun AddMediaDemo(viewModel: AppViewModel) {
    val addedMedia by viewModel.addedMedia.observeAsState()

    if (addedMedia != null) {
        MediaFilePreviewCard(addedMedia!!)
    } else {
        IntroCard()
    }

    LazyVerticalGrid(cells = GridCells.Fixed(2)) {
        item {
            Button(
                modifier = Modifier.padding(16.dp),
                onClick = { viewModel.addImage() }
            ) {
                Text(stringResource(R.string.demo_add_image_label))
            }
        }
        item {
            Button(
                modifier = Modifier.padding(16.dp),
                onClick = { viewModel.addImage() }
            ) {
                Text(stringResource(R.string.demo_add_video_label))
            }
        }
    }
}

@Composable
fun AddDownloadFileDemo(viewModel: AppViewModel) {
}
