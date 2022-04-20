/*
 * Copyright 2022 Google LLC
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
package com.google.modernstorage.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSION_GRANT_RESULTS
import androidx.core.content.ContextCompat

abstract class RequestStorageAccessFor(
    private val action: StoragePermissions.Action,
    private val fileTypes: List<StoragePermissions.FileType>,
    private val createdBy: StoragePermissions.CreatedBy
) : ActivityResultContract<Void?, Boolean>() {

    override fun createIntent(context: Context, input: Void?): Intent {
        val targetSdk = context.applicationInfo.targetSdkVersion
        val permissions =
            StoragePermissions.getPermissions(action, fileTypes, createdBy, targetSdk)
        return Intent(ACTION_REQUEST_PERMISSIONS).apply {
            putExtra(EXTRA_PERMISSIONS, permissions.toTypedArray())
        }
    }

    override fun getSynchronousResult(
        context: Context,
        input: Void?
    ): SynchronousResult<Boolean>? {
        val targetSdk = context.applicationInfo.targetSdkVersion
        val permissions =
            StoragePermissions.getPermissions(action, fileTypes, createdBy, targetSdk)

        if (permissions.isEmpty()) {
            return SynchronousResult(true)
        }

        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        return if (allGranted) SynchronousResult(true) else null
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Boolean {
        if (resultCode != Activity.RESULT_OK) return false
        if (intent == null) return false
        val permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS)
        val grantResults = intent.getIntArrayExtra(EXTRA_PERMISSION_GRANT_RESULTS)
        if (grantResults == null || permissions == null) return false

        return grantResults.all { result -> result == PackageManager.PERMISSION_GRANTED }
    }
}

class RequestStorageReadAccessFor(
    fileTypes: List<StoragePermissions.FileType>,
    createdBy: StoragePermissions.CreatedBy
) : RequestStorageAccessFor(StoragePermissions.Action.READ, fileTypes, createdBy)

class RequestStorageReadWriteAccessFor(
    fileTypes: List<StoragePermissions.FileType>,
    createdBy: StoragePermissions.CreatedBy
) : RequestStorageAccessFor(StoragePermissions.Action.READ_AND_WRITE, fileTypes, createdBy)
