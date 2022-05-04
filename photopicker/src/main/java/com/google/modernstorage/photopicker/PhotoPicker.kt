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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.ext.SdkExtensions.getExtensionVersion
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import androidx.core.os.BuildCompat
import java.util.ArrayList
import java.util.LinkedHashSet

@Deprecated(
    "Use the new PickMultipleMedia ActivityResultContract",
    ReplaceWith("PickMultipleMedia(maxItems = 5)"),
    DeprecationLevel.WARNING
)
@BuildCompat.PrereleaseSdkCheck
class PhotoPicker : ActivityResultContract<PhotoPicker.Args, List<Uri>>() {
    companion object {
        @JvmStatic
        fun isPhotoPickerAvailable(): Boolean {
            return BuildCompat.isAtLeastT()
        }

        private const val INTENT_PICK_IMAGES = "android.provider.action.PICK_IMAGES"
        private const val EXTRA_PICK_IMAGES_MAX = "android.provider.extra.PICK_IMAGES_MAX"
    }

    enum class Type {
        IMAGES_ONLY, VIDEO_ONLY, IMAGES_AND_VIDEO
    }

    class Args(val type: Type, val maxItems: Int)

    @CallSuper
    override fun createIntent(context: Context, input: Args): Intent {
        if (isPhotoPickerAvailable()) {
            val intent = Intent(INTENT_PICK_IMAGES).apply {
                if (input.maxItems > 1) {
                    putExtra(EXTRA_PICK_IMAGES_MAX, input.maxItems)
                }

                if (input.type == Type.IMAGES_ONLY) {
                    type = "image/*"
                } else if (input.type == Type.VIDEO_ONLY) {
                    type = "video/*"
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
        return if (resultCode != Activity.RESULT_OK || intent == null) emptyList() else getClipDataUris(intent)
    }

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

/**
 * An [ActivityResultContract] to use the photo picker through [MediaStore.ACTION_PICK_IMAGES]
 * when available, and else rely on ACTION_OPEN_DOCUMENT.
 *
 * The input is a [PickMediaRequest].
 *
 * The output is a `Uri` when the user has selected a media or `null` when the user hasn't
 * selected any item. Keep in mind that `Uri` returned by the photo picker isn't writable.
 *
 * This can be extended to override [createIntent] if you wish to pass additional
 * extras to the Intent created by `super.createIntent()`.
 */
@BuildCompat.PrereleaseSdkCheck
open class PickMedia : ActivityResultContract<PickMediaRequest, Uri?>() {
    companion object {
        /**
         * Check if the current device has support for the photo picker by checking the running
         * Android version or the SDK extension version
         */
        // The SuppressLint(NewApi) is due to the complex nature of SDK extensions. They're
        // available from Android 11 (API 30) but hidden when compiling with a SDK older than API 33
        @SuppressLint("NewApi")
        @JvmStatic
        fun isPhotoPickerAvailable(): Boolean {
            return when {
                BuildCompat.isAtLeastT() -> true
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> getExtensionVersion(Build.VERSION_CODES.R) >= 2
                else -> false
            }
        }

        internal fun getVisualMimeType(input: MediaType): String? {
            return when (input) {
                is ImageOnly -> "image/*"
                is VideoOnly -> "video/*"
                is SingleMimeType -> input.mimeType
                is ImageAndVideo -> null
            }
        }
    }

    /**
     * Represents filter input type accepted by the photo picker.
     */
    sealed interface MediaType

    /**
     * [MediaType] object used to filter images only when using the photo picker.
     */
    object ImageOnly : MediaType

    /**
     * [MediaType] object used to filter video only when using the photo picker.
     */
    object VideoOnly : MediaType

    /**
     * [MediaType] object used to filter images and video when using the photo picker.
     */
    object ImageAndVideo : MediaType

    /**
     * [MediaType] class used to filter a single mime type only when using the photo
     * picker.
     */
    class SingleMimeType(val mimeType: String) : MediaType

    // MediaStore.getPickImagesMaxLimit is only called when the photo picker is available but
    // it's currently complex to label it as available from Android 11 (API 30) with the right
    // mainline version
    @SuppressLint("NewApi")
    @CallSuper
    override fun createIntent(context: Context, input: PickMediaRequest): Intent {
        // Check if Photo Picker is available on the device
        return if (isPhotoPickerAvailable()) {
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = getVisualMimeType(input.mediaType)
            }
        } else {
            // For older devices running KitKat and higher and devices running Android 12
            // and 13 without the SDK extension that includes the Photo Picker, rely on the
            // ACTION_OPEN_DOCUMENT intent
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = getVisualMimeType(input.mediaType)

                if (type == null) {
                    // ACTION_OPEN_DOCUMENT requires to set this parameter when launching the
                    // intent with multiple mime types
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                }
            }
        }
    }

    @Suppress("InvalidNullability")
    final override fun getSynchronousResult(
        context: Context,
        input: PickMediaRequest
    ): SynchronousResult<Uri?>? = null

    final override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.data
    }
}

/**
 * An [ActivityResultContract] to use the Photo Picker through [MediaStore.ACTION_PICK_IMAGES]
 * when available, and else rely on ACTION_OPEN_DOCUMENT.
 *
 * The constructor accepts one parameter `maxItems` to limit the number of selectable items when
 * using the photo picker to return. Keep in mind that this parameter isn't supported on devices
 * when the photo picker isn't available.
 *
 * The input is a [PickMediaRequest].
 *
 * The output is a list `Uri` of the selected media. It can be empty if the user hasn't selected
 * any items. Keep in mind that `Uri` returned by the photo picker aren't writable.
 *
 * This can be extended to override [createIntent] if you wish to pass additional
 * extras to the Intent created by `super.createIntent()`.
 */
@BuildCompat.PrereleaseSdkCheck
open class PickMultipleMedia(
    private val maxItems: Int = getMaxItems()
) : ActivityResultContract<PickMediaRequest, List<@JvmSuppressWildcards Uri>>() {

    init {
        require(maxItems > 1) {
            "Max items must be higher than 1"
        }
    }

    @SuppressLint("NewApi")
    @CallSuper
    override fun createIntent(context: Context, input: PickMediaRequest): Intent {
        // Check to see if the photo picker is available
        return if (PickMedia.isPhotoPickerAvailable()) {
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = PickMedia.getVisualMimeType(input.mediaType)

                require(maxItems <= MediaStore.getPickImagesMaxLimit()) {
                    "Max items must be less or equals MediaStore.getPickImagesMaxLimit()"
                }

                putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxItems)
            }
        } else {
            // For older devices running KitKat and higher and devices running Android 12
            // and 13 without the SDK extension that includes the Photo Picker, rely on the
            // ACTION_OPEN_DOCUMENT intent
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = PickMedia.getVisualMimeType(input.mediaType)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

                if (type == null) {
                    // ACTION_OPEN_DOCUMENT requires to set this parameter when launching the
                    // intent with multiple mime types
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                }
            }
        }
    }

    @Suppress("InvalidNullability")
    final override fun getSynchronousResult(
        context: Context,
        input: PickMediaRequest
    ): SynchronousResult<List<@JvmSuppressWildcards Uri>>? = null

    final override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return intent.takeIf {
            resultCode == Activity.RESULT_OK
        }?.getClipDataUris() ?: emptyList()
    }

    internal companion object {
        internal fun Intent.getClipDataUris(): List<Uri> {
            // Use a LinkedHashSet to maintain any ordering that may be
            // present in the ClipData
            val resultSet = LinkedHashSet<Uri>()
            data?.let { data ->
                resultSet.add(data)
            }
            val clipData = clipData
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

        /**
         * The photo picker has a maximum limit of selectable items returned by
         * [MediaStore.getPickImagesMaxLimit()]. On devices not supporting the photo picker, the
         * limit is ignored.
         *
         * @see MediaStore.EXTRA_PICK_IMAGES_MAX
         */
        // MediaStore.getPickImagesMaxLimit is only called when the photo picker is available but
        // it's currently complex to label it as available from Android 11 (API 30) with the right
        // mainline version
        @SuppressLint("NewApi")
        internal fun getMaxItems() = if (PickMedia.isPhotoPickerAvailable()) {
            MediaStore.getPickImagesMaxLimit()
        } else {
            Integer.MAX_VALUE
        }
    }
}
