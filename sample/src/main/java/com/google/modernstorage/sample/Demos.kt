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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.ui.graphics.vector.ImageVector

data class Demo(
    val route: String,
    @StringRes val name: Int,
    @StringRes val description: Int,
    val icon: ImageVector,
)

object Demos {
    val AddMediaFile = Demo(
        route = "demo_add_media_file",
        name = R.string.demo_add_media_file_name,
        description = R.string.demo_add_media_file_description,
        icon = Icons.Filled.AddPhotoAlternate,
    )

    val CaptureMediaFile = Demo(
        route = "demo_capture_media_file",
        name = R.string.demo_capture_media_file_name,
        description = R.string.demo_capture_media_file_description,
        icon = Icons.Filled.AddAPhoto,
    )

    val AddFileToDownloads = Demo(
        route = "demo_add_file_to_downloads",
        name = R.string.demo_add_file_to_downloads_name,
        description = R.string.demo_add_file_to_downloads_description,
        icon = Icons.Filled.AddCircle,
    )

    val EditMediaFile = Demo(
        route = "demo_edit_media_file",
        name = R.string.demo_edit_media_file_name,
        description = R.string.demo_edit_media_file_description,
        icon = Icons.Filled.Edit,
    )

    val DeleteMediaFile = Demo(
        route = "demo_download_media_file",
        name = R.string.demo_delete_media_file_name,
        description = R.string.demo_delete_media_file_description,
        icon = Icons.Filled.Delete,
    )

    val ListMediaFiles = Demo(
        route = "demo_list_media_files",
        name = R.string.demo_list_media_files_name,
        description = R.string.demo_list_media_files_description,
        icon = Icons.Filled.ImageSearch,
    )

    val SelectDocumentFile = Demo(
        route = "demo_select_document_file",
        name = R.string.demo_select_document_file_name,
        description = R.string.demo_select_document_file_description,
        icon = Icons.Filled.AttachFile,
    )

    val CreateDocumentFile = Demo(
        route = "demo_create_document_file",
        name = R.string.demo_create_document_file_name,
        description = R.string.demo_create_document_file_description,
        icon = Icons.Filled.NoteAdd,
    )

    val EditDocumentFile = Demo(
        route = "demo_edit_document_file",
        name = R.string.demo_edit_document_file_name,
        description = R.string.demo_edit_document_file_description,
        icon = Icons.Filled.Edit,
    )

    val list = listOf(
        AddMediaFile,
        CaptureMediaFile,
        AddFileToDownloads,
        EditMediaFile,
        DeleteMediaFile,
        ListMediaFiles,
        SelectDocumentFile,
        CreateDocumentFile,
        EditDocumentFile,
    )
}
