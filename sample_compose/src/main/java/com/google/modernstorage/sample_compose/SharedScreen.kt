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
package com.google.modernstorage.sample_compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.modernstorage.storage.PhotoPickerArgs
import com.google.modernstorage.storage.PhotoPickerType
import com.google.modernstorage.storage.PreferredPhotoPicker
import com.google.modernstorage.storage.isPhotoPickerAvailable
import logcat.logcat

@ExperimentalMaterialApi
@Composable
fun SharedScreen(viewModel: SharedViewModel = viewModel()) {
    val context = LocalContext.current

    var uris by remember { mutableStateOf(emptyList<Uri>()) }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) {
        uris = it
    }

    val actualPhotoPickerIntent = rememberLauncherForActivityResult(ActualPhotoIntent()) {
        uris = it
    }

    fun checkAndLaunchIntent() {
        if (isPhotoPickerAvailable()) {
            actualPhotoPickerIntent.launch(PhotoPickerArgs(PhotoPickerType.PHOTOS_AND_VIDEO, 2))
        } else {
            uris = emptyList()
        }
    }

    val preferredPickerLauncher = rememberLauncherForActivityResult(PreferredPhotoPicker()) {
        uris = it
    }

    Column {
        ListItem(
            text = { Text("SDK version") },
            trailing = { Text(Build.VERSION.SDK_INT.toString()) },
        )
        Divider()

        ListItem(
            text = { Text("Release name") },
            trailing = { Text(Build.VERSION.RELEASE.toString()) },
        )
        Divider()

        ListItem(
            text = { Text("Is Photo Picker present") },
            trailing = { Text(isPhotoPickerAvailable().toString()) },
        )
        Divider()
        Spacer(Modifier.height(15.dp))

        Button(onClick = { checkAndLaunchIntent() }) {
            Text("Launch Photo Picker intent")
        }
        Spacer(Modifier.height(15.dp))

        Button(onClick = { openDocumentLauncher.launch(arrayOf("image/*", "video/*")) }) {
            Text("Launch Docs UI intent")
        }
        Spacer(Modifier.height(15.dp))

        Button(
            onClick = {
                preferredPickerLauncher.launch(
                    PhotoPickerArgs(PhotoPickerType.PHOTOS_AND_VIDEO, 2)
                )
            }
        ) {
            Text("Launch preferred picker intent")
        }
        Spacer(Modifier.height(15.dp))

        Text(uris.toString())
    }
}

private fun launchPhotoIntent(context: Context) {
    val selectImageIntent = Intent().apply {
        action = "android.provider.action.PICK_IMAGES"
        putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
        putExtra("android.provider.extra.PICK_IMAGES_MAX", 5)
    }

    context.startActivity(selectImageIntent)
}

private fun launchDocsUiIntent(context: Context) {
    val selectImageIntent = Intent().apply {
        action = "android.provider.action.PICK_IMAGES"
        putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
        putExtra("android.provider.extra.PICK_IMAGES_MAX", 5)
    }

    context.startActivity(selectImageIntent)
}

private fun launchPreferredPhotoPickerIntent(context: Context) {
    val selectImageIntent = Intent().apply {
        action = "android.provider.action.PICK_IMAGES"
        putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
        putExtra("android.provider.extra.PICK_IMAGES_MAX", 5)
    }

    context.startActivity(selectImageIntent)
}

private fun checkIntent() {
    try {
        MediaStore::class.java.getDeclaredField("ACTION_PICK_IMAGES")
        logcat("checkIntent") { "property is present" }
    } catch (e: NoSuchFieldException) {
        logcat("checkIntent") { "property is NOT present" }
    }
}
