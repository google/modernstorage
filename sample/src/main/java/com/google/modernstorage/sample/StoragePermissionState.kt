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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class StoragePermissionState private constructor(
    val canReadOwnEntries: Boolean,
    val canWriteOwnEntries: Boolean,
    val canReadSharedEntries: Boolean,
    val canWriteSharedEntries: Boolean
) {
    companion object {
        fun create(context: Context): StoragePermissionState {
            val isReadExternalStorageGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            val isWriteExternalStorageGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            return StoragePermissionState(
                canReadOwnEntries = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) true else isReadExternalStorageGranted,
                canWriteOwnEntries = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) true else isWriteExternalStorageGranted,
                canReadSharedEntries = isReadExternalStorageGranted,
                canWriteSharedEntries = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) true else isWriteExternalStorageGranted,
            )
        }
    }
}
