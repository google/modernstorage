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
package com.google.modernstorage.mediastore

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat.checkSelfPermission

fun canReadOwnEntriesInMediaStore(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        true
    } else {
        checkSelfPermission(
            context,
            WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(
            context,
            READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun canWriteOwnEntriesInMediaStore(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        true
    } else {
        checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

fun canReadSharedEntriesInMediaStore(context: Context): Boolean {
    return checkSelfPermission(
        context,
        READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(
        context,
        READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

fun canWriteSharedEntriesInMediaStore(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        canReadSharedEntriesInMediaStore(context)
    } else {
        checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}
