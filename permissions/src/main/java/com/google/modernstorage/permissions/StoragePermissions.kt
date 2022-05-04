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
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

class StoragePermissions(private val context: Context) {
    companion object {
        private const val READ_EXTERNAL_STORAGE_MASK = 0b0001
        private const val WRITE_EXTERNAL_STORAGE_MASK = 0b0011
        private const val SCOPED_STORAGE_READ_EXTERNAL_STORAGE_MASK = 0b0100
        private const val MANAGE_EXTERNAL_STORAGE_MASK = 0b1111

        private fun getPermissionMask(
            action: Action,
            types: List<FileType>,
            createdBy: CreatedBy
        ): Int {
            var permissionMask = 0

            when (createdBy) {
                CreatedBy.Self -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        permissionMask = when (action) {
                            Action.READ -> permissionMask or READ_EXTERNAL_STORAGE_MASK
                            Action.READ_AND_WRITE -> permissionMask or WRITE_EXTERNAL_STORAGE_MASK
                        }
                    }
                }
                CreatedBy.AllApps -> {
                    if (types.contains(FileType.Image)) {
                        permissionMask = when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                permissionMask or SCOPED_STORAGE_READ_EXTERNAL_STORAGE_MASK
                            }
                            else -> {
                                when (action) {
                                    Action.READ -> permissionMask or READ_EXTERNAL_STORAGE_MASK
                                    Action.READ_AND_WRITE -> permissionMask or WRITE_EXTERNAL_STORAGE_MASK
                                }
                            }
                        }
                    }

                    if (types.contains(FileType.Video)) {
                        permissionMask = when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                permissionMask or SCOPED_STORAGE_READ_EXTERNAL_STORAGE_MASK
                            }
                            else -> {
                                when (action) {
                                    Action.READ -> permissionMask or READ_EXTERNAL_STORAGE_MASK
                                    Action.READ_AND_WRITE -> permissionMask or WRITE_EXTERNAL_STORAGE_MASK
                                }
                            }
                        }
                    }

                    if (types.contains(FileType.Audio)) {
                        permissionMask = when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                permissionMask or SCOPED_STORAGE_READ_EXTERNAL_STORAGE_MASK
                            }
                            else -> {
                                when (action) {
                                    Action.READ -> permissionMask or READ_EXTERNAL_STORAGE_MASK
                                    Action.READ_AND_WRITE -> permissionMask or WRITE_EXTERNAL_STORAGE_MASK
                                }
                            }
                        }
                    }

                    if (types.contains(FileType.Document)) {
                        permissionMask = when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                permissionMask or MANAGE_EXTERNAL_STORAGE_MASK
                            }
                            else -> {
                                when (action) {
                                    Action.READ -> permissionMask or READ_EXTERNAL_STORAGE_MASK
                                    Action.READ_AND_WRITE -> permissionMask or WRITE_EXTERNAL_STORAGE_MASK
                                }
                            }
                        }
                    }
                }
            }

            return permissionMask
        }

        /**
         * Get list of required permissions for given usage
         */
        @JvmStatic
        fun getPermissions(
            action: Action,
            types: List<FileType>,
            createdBy: CreatedBy
        ): List<String> {
            val permissionMask = getPermissionMask(action, types, createdBy)
            val requiredPermissions = mutableListOf<String>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && permissionMask and MANAGE_EXTERNAL_STORAGE_MASK == MANAGE_EXTERNAL_STORAGE_MASK) {
                requiredPermissions.add(MANAGE_EXTERNAL_STORAGE)
            } else {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && permissionMask and MANAGE_EXTERNAL_STORAGE_MASK == MANAGE_EXTERNAL_STORAGE_MASK -> requiredPermissions.add(
                        MANAGE_EXTERNAL_STORAGE
                    )
                    permissionMask and SCOPED_STORAGE_READ_EXTERNAL_STORAGE_MASK == SCOPED_STORAGE_READ_EXTERNAL_STORAGE_MASK -> requiredPermissions.add(
                        READ_EXTERNAL_STORAGE
                    )
                    permissionMask and WRITE_EXTERNAL_STORAGE_MASK == WRITE_EXTERNAL_STORAGE_MASK -> requiredPermissions.add(
                        WRITE_EXTERNAL_STORAGE
                    )
                    permissionMask and READ_EXTERNAL_STORAGE_MASK == READ_EXTERNAL_STORAGE_MASK -> requiredPermissions.add(
                        READ_EXTERNAL_STORAGE
                    )
                }
            }

            return requiredPermissions
        }
    }

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

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if app can access shared files
     */
    fun hasAccess(action: Action, types: List<FileType>, createdBy: CreatedBy): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            return true
        }

        return getPermissions(action, types, createdBy).all { hasPermission(it) }
    }
}
