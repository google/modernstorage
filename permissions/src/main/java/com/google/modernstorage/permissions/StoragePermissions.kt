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

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.BuildCompat

@BuildCompat.PrereleaseSdkCheck
class StoragePermissions(private val context: Context) {
    companion object {
        private const val VISUAL_PERMISSION = "READ_MEDIA_IMAGES_VIDEO"
        private const val AUDIO_PERMISSION = "READ_MEDIA_AUDIO"

        // File Types
        enum class FileType {
            Image, Video, Audio, Document
        }

        // Ownership type
        enum class Ownership {
            Self, AllApps
        }

        // Ownership type
        enum class Action {
            READ, READ_AND_WRITE
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkFullStoragePermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    /**
     * Check if app can read shared files
     */
    fun canAccessFiles(action: Action, types: List<FileType>, ownership: Ownership): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && checkFullStoragePermission()) {
            return true
        }

        val conditions = mutableListOf<Boolean>()
        when (ownership) {
            Ownership.Self -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    conditions.add(true)
                } else {
                    when (action) {
                        Action.READ -> conditions.add(checkPermission(READ_EXTERNAL_STORAGE) || checkPermission(WRITE_EXTERNAL_STORAGE))
                        Action.READ_AND_WRITE -> conditions.add(checkPermission(WRITE_EXTERNAL_STORAGE))
                    }
                }
            }
            Ownership.AllApps -> {
                if (types.contains(FileType.Image)) {
                    when {
                        BuildCompat.isAtLeastT() -> {
                            conditions.add(checkPermission(VISUAL_PERMISSION))
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                            conditions.add(checkPermission(READ_EXTERNAL_STORAGE))
                        }
                        else -> {
                            when (action) {
                                Action.READ -> conditions.add(checkPermission(READ_EXTERNAL_STORAGE) || checkPermission(WRITE_EXTERNAL_STORAGE))
                                Action.READ_AND_WRITE -> conditions.add(checkPermission(WRITE_EXTERNAL_STORAGE))
                            }
                        }
                    }
                }

                if (types.contains(FileType.Video)) {
                    when {
                        BuildCompat.isAtLeastT() -> {
                            conditions.add(checkPermission(VISUAL_PERMISSION))
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                            conditions.add(checkPermission(READ_EXTERNAL_STORAGE))
                        }
                        else -> {
                            when (action) {
                                Action.READ -> conditions.add(checkPermission(READ_EXTERNAL_STORAGE) || checkPermission(WRITE_EXTERNAL_STORAGE))
                                Action.READ_AND_WRITE -> conditions.add(checkPermission(WRITE_EXTERNAL_STORAGE))
                            }
                        }
                    }
                }

                if (types.contains(FileType.Audio)) {
                    when {
                        BuildCompat.isAtLeastT() -> {
                            conditions.add(checkPermission(AUDIO_PERMISSION))
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                            conditions.add(checkPermission(READ_EXTERNAL_STORAGE))
                        }
                        else -> {
                            when (action) {
                                Action.READ -> conditions.add(checkPermission(READ_EXTERNAL_STORAGE) || checkPermission(WRITE_EXTERNAL_STORAGE))
                                Action.READ_AND_WRITE -> conditions.add(checkPermission(WRITE_EXTERNAL_STORAGE))
                            }
                        }
                    }
                }

                if (types.contains(FileType.Document)) {
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                            conditions.add(false)
                        }
                        else -> {
                            when (action) {
                                Action.READ -> conditions.add(checkPermission(READ_EXTERNAL_STORAGE) || checkPermission(WRITE_EXTERNAL_STORAGE))
                                Action.READ_AND_WRITE -> conditions.add(checkPermission(WRITE_EXTERNAL_STORAGE))
                            }
                        }
                    }
                }
            }
        }

        return conditions.all { it }
    }
}
