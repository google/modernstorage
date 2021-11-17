package com.google.modernstorage.storage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import java.util.ArrayList
import java.util.LinkedHashSet

class PickMedia : ActivityResultContract<Array<String>, List<Uri>>() {

    override fun createIntent(context: Context, input: Array<String>): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT)
            .putExtra(Intent.EXTRA_MIME_TYPES, input)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .setType("*/*")
    }

    override fun getSynchronousResult(
        context: Context,
        input: Array<String>
    ): SynchronousResult<List<Uri>>? = null

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return emptyList()
//        if (resultCode == Activity.RESULT_OK) {
//            if(intent != null) {
//                return intent.clipData
//            }
//        }
//        return intent.takeIf {
//            resultCode == Activity.RESULT_OK
//        }?.getClipDataUris() ?: emptyList()
    }
}

enum class PhotoPickerType {
    PHOTOS_ONLY, VIDEO_ONLY, PHOTOS_AND_VIDEO
}

data class PhotoPickerArgs(val type: PhotoPickerType = PhotoPickerType.PHOTOS_AND_VIDEO, val
limit: Int)


class PreferredPhotoPicker : ActivityResultContract<PhotoPickerArgs, List<Uri>>() {
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
        if(isPhotoPickerAvailable()) {
            val intent = Intent("android.provider.action.PICK_IMAGES").apply {
                putExtra("android.provider.extra.PICK_IMAGES_MAX", input.limit)
                when(input.type) {
                    PhotoPickerType.PHOTOS_ONLY ->
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
                    PhotoPickerType.VIDEO_ONLY ->
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                    PhotoPickerType.PHOTOS_AND_VIDEO ->
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                }
            }

            return intent
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                type = "*/*"

                when(input.type) {
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
    }

    override fun getSynchronousResult(
        context: Context,
        input: PhotoPickerArgs
    ): SynchronousResult<List<Uri>>? {
        return null
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return if (resultCode != Activity.RESULT_OK || intent == null) emptyList() else getClipDataUris(intent)
    }
}
