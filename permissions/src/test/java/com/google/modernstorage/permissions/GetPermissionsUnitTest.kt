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
import android.os.Build
import com.google.modernstorage.permissions.StoragePermissions.Action.READ
import com.google.modernstorage.permissions.StoragePermissions.Action.READ_AND_WRITE
import com.google.modernstorage.permissions.StoragePermissions.Companion.getPermissions
import com.google.modernstorage.permissions.StoragePermissions.CreatedBy.AllApps
import com.google.modernstorage.permissions.StoragePermissions.CreatedBy.Self
import com.google.modernstorage.permissions.StoragePermissions.FileType.Audio
import com.google.modernstorage.permissions.StoragePermissions.FileType.Document
import com.google.modernstorage.permissions.StoragePermissions.FileType.Image
import com.google.modernstorage.permissions.StoragePermissions.FileType.Video
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class GetPermissionsUnitTest {
    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.P])
    fun getStoragePermissions_preScopedStorage() {
        assertEquals(getPermissions(READ, listOf(Image), Self), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Video), Self), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Audio), Self), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Document), Self), listOf(READ_EXTERNAL_STORAGE))

        assertEquals(getPermissions(READ_AND_WRITE, listOf(Image), Self), listOf(WRITE_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Video), Self), listOf(WRITE_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Audio), Self), listOf(WRITE_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Document), Self), listOf(WRITE_EXTERNAL_STORAGE))

        assertEquals(getPermissions(READ, listOf(Image), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Video), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Audio), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Document), AllApps), listOf(READ_EXTERNAL_STORAGE))

        assertEquals(getPermissions(READ_AND_WRITE, listOf(Image), AllApps), listOf(WRITE_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Video), AllApps), listOf(WRITE_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Audio), AllApps), listOf(WRITE_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Document), AllApps), listOf(WRITE_EXTERNAL_STORAGE))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun getStoragePermissions_transitioningIntoScopedStorage() {
        assertEquals(getPermissions(READ, listOf(Image), Self), emptyList<String>())
        assertEquals(getPermissions(READ, listOf(Video), Self), emptyList<String>())
        assertEquals(getPermissions(READ, listOf(Audio), Self), emptyList<String>())
        assertEquals(getPermissions(READ, listOf(Document), Self), emptyList<String>())

        assertEquals(getPermissions(READ_AND_WRITE, listOf(Image), Self), emptyList<String>())
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Video), Self), emptyList<String>())
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Audio), Self), emptyList<String>())
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Document), Self), emptyList<String>())

        assertEquals(getPermissions(READ, listOf(Image), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Video), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Audio), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Document), AllApps), listOf(READ_EXTERNAL_STORAGE))

        assertEquals(getPermissions(READ_AND_WRITE, listOf(Image), AllApps), listOf(WRITE_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Video), AllApps), listOf(WRITE_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Audio), AllApps), listOf(WRITE_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Document), AllApps), listOf(WRITE_EXTERNAL_STORAGE))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R, Build.VERSION_CODES.S])
    fun getStoragePermissions_postScopedStorage() {
        assertEquals(getPermissions(READ, listOf(Image), Self), emptyList<String>())
        assertEquals(getPermissions(READ, listOf(Video), Self), emptyList<String>())
        assertEquals(getPermissions(READ, listOf(Audio), Self), emptyList<String>())
        assertEquals(getPermissions(READ, listOf(Document), Self), emptyList<String>())

        assertEquals(getPermissions(READ_AND_WRITE, listOf(Image), Self), emptyList<String>())
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Video), Self), emptyList<String>())
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Audio), Self), emptyList<String>())
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Document), Self), emptyList<String>())

        assertEquals(getPermissions(READ, listOf(Image), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Video), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Audio), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ, listOf(Document), AllApps), listOf(MANAGE_EXTERNAL_STORAGE))

        assertEquals(getPermissions(READ_AND_WRITE, listOf(Image), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Video), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Audio), AllApps), listOf(READ_EXTERNAL_STORAGE))
        assertEquals(getPermissions(READ_AND_WRITE, listOf(Document), AllApps), listOf(MANAGE_EXTERNAL_STORAGE))
    }
}
