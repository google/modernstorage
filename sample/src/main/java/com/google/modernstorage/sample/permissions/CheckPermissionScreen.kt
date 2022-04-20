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
package com.google.modernstorage.sample.permissions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.modernstorage.permissions.StoragePermissions.Action
import com.google.modernstorage.permissions.StoragePermissions.CreatedBy
import com.google.modernstorage.permissions.StoragePermissions.FileType
import com.google.modernstorage.permissions.StoragePermissions.hasStorageReadAccessFor
import com.google.modernstorage.permissions.StoragePermissions.hasStorageReadWriteAccessFor
import com.google.modernstorage.sample.Demos
import com.google.modernstorage.sample.HomeRoute
import com.google.modernstorage.sample.R

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun CheckPermissionScreen(navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Demos.CheckPermission.name)) },
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
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                item {
                    ListItem(
                        icon = { Icon(Icons.Filled.Image, contentDescription = null) },
                        text = { Text(stringResource(R.string.demo_check_permission_image_type)) },
                        secondaryText = {
                            Column(Modifier.padding(bottom = 10.dp)) {
                                CreatedByLabel(
                                    action = Action.READ,
                                    createdBy = CreatedBy.Self,
                                    enabled = context.hasStorageReadAccessFor(
                                        listOf(FileType.Image),
                                        CreatedBy.Self
                                    )
                                )
                                CreatedByLabel(
                                    action = Action.READ,
                                    createdBy = CreatedBy.AllApps,
                                    enabled = context.hasStorageReadAccessFor(
                                        listOf(FileType.Image),
                                        CreatedBy.AllApps
                                    )
                                )
                                Spacer(Modifier.height(10.dp))
                                CreatedByLabel(
                                    action = Action.READ_AND_WRITE,
                                    createdBy = CreatedBy.Self,
                                    enabled = context.hasStorageReadWriteAccessFor(
                                        listOf(FileType.Image),
                                        CreatedBy.Self
                                    )
                                )
                                CreatedByLabel(
                                    action = Action.READ_AND_WRITE,
                                    createdBy = CreatedBy.AllApps,
                                    enabled = context.hasStorageReadWriteAccessFor(
                                        listOf(FileType.Image),
                                        CreatedBy.AllApps
                                    )
                                )
                            }
                        },
                    )
                    Divider()
                    ListItem(
                        icon = { Icon(Icons.Filled.Movie, contentDescription = null) },
                        text = { Text(stringResource(R.string.demo_check_permission_video_type)) },
                        secondaryText = {
                            Column(Modifier.padding(bottom = 10.dp)) {
                                CreatedByLabel(
                                    action = Action.READ,
                                    createdBy = CreatedBy.Self,
                                    enabled = context.hasStorageReadAccessFor(
                                        listOf(FileType.Video),
                                        CreatedBy.Self
                                    )
                                )
                                CreatedByLabel(
                                    action = Action.READ,
                                    createdBy = CreatedBy.AllApps,
                                    enabled = context.hasStorageReadAccessFor(
                                        listOf(FileType.Video),
                                        CreatedBy.AllApps
                                    )
                                )
                                Spacer(Modifier.height(10.dp))
                                CreatedByLabel(
                                    action = Action.READ_AND_WRITE,
                                    createdBy = CreatedBy.Self,
                                    enabled = context.hasStorageReadWriteAccessFor(
                                        listOf(FileType.Video),
                                        CreatedBy.Self
                                    )
                                )
                                CreatedByLabel(
                                    action = Action.READ_AND_WRITE,
                                    createdBy = CreatedBy.AllApps,
                                    enabled = context.hasStorageReadWriteAccessFor(
                                        listOf(FileType.Video),
                                        CreatedBy.AllApps
                                    )
                                )
                            }
                        },
                    )
                    Divider()
                    ListItem(
                        icon = { Icon(Icons.Filled.MusicNote, contentDescription = null) },
                        text = { Text(stringResource(R.string.demo_check_permission_audio_type)) },
                        secondaryText = {
                            Column(Modifier.padding(bottom = 10.dp)) {
                                CreatedByLabel(
                                    action = Action.READ,
                                    createdBy = CreatedBy.Self,
                                    enabled = context.hasStorageReadAccessFor(
                                        listOf(FileType.Audio),
                                        CreatedBy.Self
                                    )
                                )
                                CreatedByLabel(
                                    action = Action.READ,
                                    createdBy = CreatedBy.AllApps,
                                    enabled = context.hasStorageReadAccessFor(
                                        listOf(FileType.Audio),
                                        CreatedBy.AllApps
                                    )
                                )
                                Spacer(Modifier.height(10.dp))
                                CreatedByLabel(
                                    action = Action.READ_AND_WRITE,
                                    createdBy = CreatedBy.Self,
                                    enabled = context.hasStorageReadWriteAccessFor(
                                        listOf(FileType.Audio),
                                        CreatedBy.Self
                                    )
                                )
                                CreatedByLabel(
                                    action = Action.READ_AND_WRITE,
                                    createdBy = CreatedBy.AllApps,
                                    enabled = context.hasStorageReadWriteAccessFor(
                                        listOf(FileType.Audio),
                                        CreatedBy.AllApps
                                    )
                                )
                            }
                        },
                    )
                    Divider()
                    ListItem(
                        icon = { Icon(Icons.Filled.Article, contentDescription = null) },
                        text = { Text(stringResource(R.string.demo_check_permission_document_type)) },
                        secondaryText = {
                            Column(Modifier.padding(bottom = 10.dp)) {
                                CreatedByLabel(
                                    action = Action.READ,
                                    createdBy = CreatedBy.Self,
                                    enabled = context.hasStorageReadAccessFor(
                                        listOf(FileType.Document),
                                        CreatedBy.Self
                                    )
                                )
                                CreatedByLabel(
                                    action = Action.READ,
                                    createdBy = CreatedBy.AllApps,
                                    enabled = context.hasStorageReadAccessFor(
                                        listOf(FileType.Document),
                                        CreatedBy.AllApps
                                    )
                                )
                                Spacer(Modifier.height(10.dp))
                                CreatedByLabel(
                                    action = Action.READ_AND_WRITE,
                                    createdBy = CreatedBy.Self,
                                    enabled = context.hasStorageReadWriteAccessFor(
                                        listOf(FileType.Document),
                                        CreatedBy.Self
                                    )
                                )
                                CreatedByLabel(
                                    action = Action.READ_AND_WRITE,
                                    createdBy = CreatedBy.AllApps,
                                    enabled = context.hasStorageReadWriteAccessFor(
                                        listOf(FileType.Document),
                                        CreatedBy.AllApps
                                    )
                                )
                            }
                        },
                    )
                    Divider()
                }
            }
        }
    )
}

@Composable
fun CreatedByLabel(action: Action, createdBy: CreatedBy, enabled: Boolean) {
    val textContent = buildAnnotatedString {
        append(if (enabled) "✅ " else "❌ ")
        when (action) {
            Action.READ -> append(stringResource(R.string.demo_check_permission_read_label))
            Action.READ_AND_WRITE -> append(stringResource(R.string.demo_check_permission_read_and_write_label))
        }

        append(" ")
        append(stringResource(R.string.demo_check_permission_created_by_label))
        append(" ")

        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
            when (createdBy) {
                CreatedBy.Self -> append(stringResource(R.string.demo_check_permission_created_by_self_label))
                CreatedBy.AllApps -> append(stringResource(R.string.demo_check_permission_created_by_all_apps_label))
            }
        }
    }

    Text(textContent)
}
