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

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.modernstorage.permissions.StoragePermissions
import com.google.modernstorage.sample.Demos
import com.google.modernstorage.sample.HomeRoute
import com.google.modernstorage.sample.R
import com.google.modernstorage.sample.mediastore.MediaStoreViewModel.MediaType
import com.google.modernstorage.sample.ui.shared.MediaPreviewCard
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@Composable
fun AddMediaScreen(navController: NavController, viewModel: MediaStoreViewModel = viewModel()) {
    val addedFile by viewModel.addedFile.collectAsState()
    var openPermissionDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val permissions = StoragePermissions(LocalContext.current)
    val toastMessage = stringResource(R.string.authorization_dialog_success_toast)
    val requestPermission =
        rememberLauncherForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(toastMessage)
                }
            }
        }

    if (openPermissionDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.authorization_dialog_title)) },
            text = { Text(stringResource(R.string.authorization_dialog_add_files_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        openPermissionDialog = false
                        requestPermission.launch(WRITE_EXTERNAL_STORAGE)
                    }
                ) {
                    Text(stringResource(R.string.authorization_dialog_confirm_label))
                }
            },
            onDismissRequest = { openPermissionDialog = false },
            dismissButton = {
                TextButton(onClick = { openPermissionDialog = false }) {
                    Text(stringResource(R.string.authorization_dialog_cancel_label))
                }
            }
        )
    }

    fun checkAndRequestStoragePermission(onSuccess: () -> Unit,) {
        val isGranted = permissions.canReadAndWriteFiles(
            types = listOf(
                StoragePermissions.FileType.Image,
                StoragePermissions.FileType.Video,
                StoragePermissions.FileType.Audio
            ),
            createdBy = StoragePermissions.CreatedBy.Self
        )

        if (isGranted) {
            onSuccess()
        } else {
            openPermissionDialog = true
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
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
            LazyColumn(Modifier.padding(paddingValues)) {
                item {
                    Button(
                        modifier = Modifier.padding(4.dp).fillMaxWidth(),
                        onClick = {
                            checkAndRequestStoragePermission { viewModel.addMedia(MediaType.IMAGE) }
                        }
                    ) {
                        Text(stringResource(R.string.demo_add_image_label))
                    }
                }
                item {
                    Button(
                        modifier = Modifier.padding(4.dp).fillMaxWidth(),
                        onClick = {
                            checkAndRequestStoragePermission { viewModel.addMedia(MediaType.VIDEO) }
                        }
                    ) {
                        Text(stringResource(R.string.demo_add_video_label))
                    }
                }
                item {
                    Button(
                        modifier = Modifier.padding(4.dp).fillMaxWidth(),
                        onClick = {
                            checkAndRequestStoragePermission { viewModel.addMedia(MediaType.AUDIO) }
                        }
                    ) {
                        Text(stringResource(R.string.demo_add_audio_label))
                    }
                }

                addedFile?.let {
                    item {
                        MediaPreviewCard(it)
                    }
                }
            }
        }
    )
}
