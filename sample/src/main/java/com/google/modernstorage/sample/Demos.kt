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

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

data class Demo(
    val route: String,
    @StringRes val name: Int,
    @StringRes val description: Int,
    val icon: ImageVector,
)

object Demos {
    val CheckPermissions = Demo(
        route = "demo_check_permissions",
        name = R.string.demo_check_permissions_name,
        description = R.string.demo_check_permissions_description,
        icon = Icons.Filled.Lock,
    )

    val AddMedia = Demo(
        route = "demo_add_media_file",
        name = R.string.demo_add_media_name,
        description = R.string.demo_add_media_description,
        icon = Icons.Filled.AddPhotoAlternate,
    )

    val CaptureMedia = Demo(
        route = "demo_capture_media_file",
        name = R.string.demo_capture_media_name,
        description = R.string.demo_capture_media_description,
        icon = Icons.Filled.AddAPhoto,
    )

    val AddFileToDownloads = Demo(
        route = "demo_add_file_to_downloads",
        name = R.string.demo_add_file_to_downloads_name,
        description = R.string.demo_add_file_to_downloads_description,
        icon = Icons.Filled.AddCircle,
    )

    val ListMedia = Demo(
        route = "demo_list_media_files",
        name = R.string.demo_list_media_name,
        description = R.string.demo_list_media_description,
        icon = Icons.Filled.ImageSearch,
    )

    val SelectDocument = Demo(
        route = "demo_select_document",
        name = R.string.demo_select_document_name,
        description = R.string.demo_select_document_description,
        icon = Icons.Filled.AttachFile,
    )

    val EditDocument = Demo(
        route = "demo_edit_document",
        name = R.string.demo_edit_document_name,
        description = R.string.demo_edit_document_description,
        icon = Icons.Filled.NoteAdd,
    )

    val PickVisualMedia = Demo(
        route = "demo_pick_visual_media",
        name = R.string.demo_pick_visual_media_name,
        description = R.string.demo_pick_visual_media_description,
        icon = Icons.Filled.PhotoLibrary,
    )

    val list = listOf(
        CheckPermissions,
        AddMedia,
        CaptureMedia,
        AddFileToDownloads,
        ListMedia,
        SelectDocument,
        EditDocument,
        PickVisualMedia,
    )
}
