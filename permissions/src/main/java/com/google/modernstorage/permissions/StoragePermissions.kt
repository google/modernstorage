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
import androidx.core.content.ContextCompat

class StoragePermissions(private val context: Context) {
    companion object {
        private const val READ_EXTERNAL_STORAGE_MASK = 0b000001
        private const val WRITE_EXTERNAL_STORAGE_MASK = 0b000011
        private const val READ_IMAGES_MASK = 0b000100
        private const val READ_VIDEO_MASK = 0b001000
        private const val READ_AUDIO_MASK = 0b010000
        private const val MANAGE_EXTERNAL_STORAGE_MASK = 0b111111

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
                            Action.READ -> permissionMask or READ_EXTERNAL_STORAGE_MASK or WRITE_EXTERNAL_STORAGE_MASK
                            Action.READ_AND_WRITE -> permissionMask or WRITE_EXTERNAL_STORAGE_MASK
                        }
                    }
                }
                CreatedBy.AllApps -> {
                    if (types.contains(FileType.Image)) {
                        permissionMask = when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                permissionMask or READ_EXTERNAL_STORAGE_MASK
                            }
                            else -> {
                                when (action) {
                                    Action.READ -> permissionMask or READ_EXTERNAL_STORAGE_MASK or WRITE_EXTERNAL_STORAGE_MASK
                                    Action.READ_AND_WRITE -> permissionMask or WRITE_EXTERNAL_STORAGE_MASK
                                }
                            }
                        }
                    }

                    if (types.contains(FileType.Video)) {
                        permissionMask = when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                permissionMask or READ_EXTERNAL_STORAGE_MASK
                            }
                            else -> {
                                when (action) {
                                    Action.READ -> permissionMask or READ_EXTERNAL_STORAGE_MASK or WRITE_EXTERNAL_STORAGE_MASK
                                    Action.READ_AND_WRITE -> permissionMask or WRITE_EXTERNAL_STORAGE_MASK
                                }
                            }
                        }
                    }

                    if (types.contains(FileType.Audio)) {
                        permissionMask = when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                permissionMask or READ_EXTERNAL_STORAGE_MASK
                            }
                            else -> {
                                when (action) {
                                    Action.READ -> permissionMask or READ_EXTERNAL_STORAGE_MASK or WRITE_EXTERNAL_STORAGE_MASK
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
                                    Action.READ -> permissionMask or READ_EXTERNAL_STORAGE_MASK or WRITE_EXTERNAL_STORAGE_MASK
                                    Action.READ_AND_WRITE -> permissionMask or WRITE_EXTERNAL_STORAGE_MASK
                                }
                            }
                        }
                    }
                }
            }

            return permissionMask
        }

        private fun getRequiredPermissions(
            action: Action,
            types: List<FileType>,
            createdBy: CreatedBy
        ): List<String> {
            val permissionMask = getPermissionMask(action, types, createdBy)

            val requiredPermissions = mutableListOf<String>()
            if (permissionMask and READ_EXTERNAL_STORAGE_MASK > 1) requiredPermissions.add(
                READ_EXTERNAL_STORAGE
            )
            if (permissionMask and WRITE_EXTERNAL_STORAGE_MASK > 1) requiredPermissions.add(
                WRITE_EXTERNAL_STORAGE
            )

            return requiredPermissions
        }

        /**
         * Get list of required permissions for given read usage
         */
        fun getReadFilesPermissions(types: List<FileType>, createdBy: CreatedBy): List<String> {
            return getRequiredPermissions(Action.READ, types, createdBy)
        }

        /**
         * Get list of required permissions for given read usage
         */
        fun getReadAndWriteFilesPermissions(types: List<FileType>, createdBy: CreatedBy): List<String> {
            return getRequiredPermissions(Action.READ_AND_WRITE, types, createdBy)
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
     * Check if app can read shared files
     */
    private fun canAccessFiles(
        action: Action,
        types: List<FileType>,
        createdBy: CreatedBy
    ): Boolean {
        val grantedPermissionMask =
            if (hasPermission(READ_EXTERNAL_STORAGE)) READ_EXTERNAL_STORAGE_MASK else 0 or
                if (hasPermission(WRITE_EXTERNAL_STORAGE)) WRITE_EXTERNAL_STORAGE_MASK else 0 or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) MANAGE_EXTERNAL_STORAGE_MASK else 0

        return (getPermissionMask(action, types, createdBy) and grantedPermissionMask) > 0
    }

    /**
     * Check if app can read shared files
     */
    fun canReadFiles(types: List<FileType>, createdBy: CreatedBy): Boolean {
        return canAccessFiles(Action.READ, types, createdBy)
    }

    /**
     * Check if app can read and write shared files
     */
    fun canReadAndWriteFiles(types: List<FileType>, createdBy: CreatedBy): Boolean {
        return canAccessFiles(Action.READ_AND_WRITE, types, createdBy)
    }
}
