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
package com.google.modernstorage.photopicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import androidx.core.os.BuildCompat
import java.util.ArrayList
import java.util.LinkedHashSet

@BuildCompat.PrereleaseSdkCheck
class PhotoPicker : ActivityResultContract<PhotoPicker.Args, List<Uri>>() {
    companion object {
        fun isPhotoPickerAvailable(): Boolean {
            return BuildCompat.isAtLeastT()
        }

        private const val INTENT_PICK_IMAGES = "android.provider.action.PICK_IMAGES"
        private const val EXTRA_PICK_IMAGES_MAX = "android.provider.extra.PICK_IMAGES_MAX"

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

    enum class Type {
        IMAGES_ONLY, VIDEO_ONLY, IMAGES_AND_VIDEO
    }

    class Args(val type: Type, val maxItems: Int)

    @CallSuper
    override fun createIntent(context: Context, input: Args): Intent {
        if (isPhotoPickerAvailable()) {
            val intent = Intent(INTENT_PICK_IMAGES).apply {
                putExtra(EXTRA_PICK_IMAGES_MAX, input.maxItems)
                when (input.type) {
                    Type.IMAGES_ONLY ->
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
                    Type.VIDEO_ONLY ->
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                    Type.IMAGES_AND_VIDEO ->
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                }
            }

            return intent
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"

                if (input.maxItems > 1) {
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }

                when (input.type) {
                    Type.IMAGES_ONLY ->
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
                    Type.VIDEO_ONLY ->
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                    Type.IMAGES_AND_VIDEO ->
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                }
            }

            return intent
        }
    }

    override fun getSynchronousResult(context: Context, input: Args): SynchronousResult<List<Uri>>? {
        return null
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        // TODO: Show toast when higher number of items have been selected by the user when using
        //  ACTION_OPEN_DOCUMENT

        return if (resultCode != Activity.RESULT_OK || intent == null) emptyList() else getClipDataUris(intent)
    }
}
