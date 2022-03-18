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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.EXTRA_PERMISSION_GRANT_RESULTS
import androidx.core.content.ContextCompat

class RequestAccess : ActivityResultContract<RequestAccess.Args, Boolean>() {
    class Args(
        val action: StoragePermissions.Action,
        val types: List<StoragePermissions.FileType>,
        val createdBy: StoragePermissions.CreatedBy
    )

    override fun createIntent(context: Context, input: Args): Intent {
        val permissions =
            StoragePermissions.getPermissions(input.action, input.types, input.createdBy)
        return Intent(ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS).putExtra(
            ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS,
            permissions.toTypedArray()
        )
    }

    override fun getSynchronousResult(
        context: Context,
        input: Args
    ): SynchronousResult<Boolean>? {
        val permissions =
            StoragePermissions.getPermissions(input.action, input.types, input.createdBy)

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
        val permissions =
            intent.getStringArrayExtra(ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS)
        val grantResults = intent.getIntArrayExtra(EXTRA_PERMISSION_GRANT_RESULTS)
        if (grantResults == null || permissions == null) return false

        return grantResults.all { result -> result == PackageManager.PERMISSION_GRANTED }
    }
}
