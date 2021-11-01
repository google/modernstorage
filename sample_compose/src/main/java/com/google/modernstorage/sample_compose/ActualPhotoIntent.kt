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
package com.google.modernstorage.sample_compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import com.google.modernstorage.storage.PhotoPickerArgs
import com.google.modernstorage.storage.PhotoPickerType
import java.util.ArrayList
import java.util.LinkedHashSet

class ActualPhotoIntent : ActivityResultContract<PhotoPickerArgs, List<Uri>>() {
    companion object {
        private fun getClipDataUris(intent: Intent): List<Uri> {
            // Use a LinkedHashSet to maintain any ordering that may be
            // present in the ClipData
            val resultSet = LinkedHashSet<Uri>()
            if (intent.data != null) {
                resultSet.add(intent.data!!)
            }
            val clipData = intent.clipData
            if (clipData == null && resultSet.isEmpty()) {
                return emptyList()
            } else if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    if (uri != null) {
                        resultSet.add(uri)
                    }
                }
            }
            return ArrayList(resultSet)
        }
    }

    @CallSuper
    override fun createIntent(context: Context, input: PhotoPickerArgs): Intent {
        val intent = Intent("android.provider.action.PICK_IMAGES").apply {
            putExtra("android.provider.extra.PICK_IMAGES_MAX", input.limit)
            when (input.type) {
                PhotoPickerType.PHOTOS_ONLY ->
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
                PhotoPickerType.VIDEO_ONLY ->
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                PhotoPickerType.PHOTOS_AND_VIDEO ->
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            }
        }

        return intent
    }

    override fun getSynchronousResult(
        context: Context,
        input: PhotoPickerArgs
    ): SynchronousResult<List<Uri>>? {
        return null
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return if (resultCode != Activity.RESULT_OK || intent == null) emptyList() else getClipDataUris(
            intent
        )
    }
}
