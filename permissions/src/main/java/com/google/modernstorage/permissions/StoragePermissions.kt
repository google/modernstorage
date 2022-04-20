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
package com.google.modernstorage.permissions

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_IMAGE
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

object StoragePermissions {
    /**
     * Type of files
     */
    enum class FileType {
        Image, Video, Audio, Document
    }

    /**
     * Type of file ownership
     */
    enum class CreatedBy {
        Self, AllApps
    }

    /**
     * Type of file actions
     */
    enum class Action {
        READ, READ_AND_WRITE
    }

    /**
     * Check if app can read shared files
     */
    fun Context.hasStorageReadAccessFor(
        fileTypes: List<FileType>,
        createdBy: CreatedBy
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            return true
        }

        val targetSdk = applicationInfo.targetSdkVersion

        return getPermissions(Action.READ, fileTypes, createdBy, targetSdk).all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if app can read & write shared files
     */
    fun Context.hasStorageReadWriteAccessFor(
        fileTypes: List<FileType>,
        createdBy: CreatedBy
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            return true
        }

        val targetSdk = applicationInfo.targetSdkVersion

        return getPermissions(Action.READ_AND_WRITE, fileTypes, createdBy, targetSdk).all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    internal fun getPermissions(
        action: Action,
        types: List<FileType>,
        createdBy: CreatedBy,
        targetSdk: Int
    ): Set<String> {
        val permissions = mutableSetOf<String>()

        when (createdBy) {
            CreatedBy.Self -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    permissions += when (action) {
                        Action.READ -> READ_EXTERNAL_STORAGE
                        Action.READ_AND_WRITE -> WRITE_EXTERNAL_STORAGE
                    }
                }
            }
            CreatedBy.AllApps -> {
                if (types.contains(FileType.Image)) {
                    permissions += when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && targetSdk >= Build.VERSION_CODES.TIRAMISU -> {
                            READ_MEDIA_IMAGE
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                            READ_EXTERNAL_STORAGE
                        }
                        else -> {
                            when (action) {
                                Action.READ -> READ_EXTERNAL_STORAGE
                                Action.READ_AND_WRITE -> WRITE_EXTERNAL_STORAGE
                            }
                        }
                    }
                }

                if (types.contains(FileType.Video)) {
                    permissions += when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && targetSdk >= Build.VERSION_CODES.TIRAMISU -> {
                            READ_MEDIA_VIDEO
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                            READ_EXTERNAL_STORAGE
                        }
                        else -> {
                            when (action) {
                                Action.READ -> READ_EXTERNAL_STORAGE
                                Action.READ_AND_WRITE -> WRITE_EXTERNAL_STORAGE
                            }
                        }
                    }
                }

                if (types.contains(FileType.Audio)) {
                    permissions += when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && targetSdk >= Build.VERSION_CODES.TIRAMISU -> {
                            READ_MEDIA_AUDIO
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                            READ_EXTERNAL_STORAGE
                        }
                        else -> {
                            when (action) {
                                Action.READ -> READ_EXTERNAL_STORAGE
                                Action.READ_AND_WRITE -> WRITE_EXTERNAL_STORAGE
                            }
                        }
                    }
                }

                if (types.contains(FileType.Document)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        return setOf(MANAGE_EXTERNAL_STORAGE)
                    } else {
                        permissions += when (action) {
                            Action.READ -> READ_EXTERNAL_STORAGE
                            Action.READ_AND_WRITE -> WRITE_EXTERNAL_STORAGE
                        }
                    }
                }
            }
        }

        return permissions
    }
}
