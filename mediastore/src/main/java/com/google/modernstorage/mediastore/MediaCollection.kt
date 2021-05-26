/*
 * Copyright 2021 The Android Open Source Project
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
package com.google.modernstorage.mediastore

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi

fun getImageCollection(location: StorageLocation): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getImageCollectionApi29(location)
    } else {
        getImageCollectionApi21(location)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun getImageCollectionApi29(location: StorageLocation): Uri? {
    return when (location) {
        Internal -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_INTERNAL)
        SharedPrimary -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }
}

private fun getImageCollectionApi21(location: StorageLocation): Uri? {
    return when (location) {
        Internal -> MediaStore.Images.Media.INTERNAL_CONTENT_URI
        SharedPrimary -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
}

fun getVideoCollection(location: StorageLocation): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getVideoCollectionApi29(location)
    } else {
        getVideoCollectionApi21(location)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun getVideoCollectionApi29(location: StorageLocation): Uri? {
    return when (location) {
        Internal -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_INTERNAL)
        SharedPrimary -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }
}

private fun getVideoCollectionApi21(location: StorageLocation): Uri? {
    return when (location) {
        Internal -> MediaStore.Video.Media.INTERNAL_CONTENT_URI
        SharedPrimary -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }
}
