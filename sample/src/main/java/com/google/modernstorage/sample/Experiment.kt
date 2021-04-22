package com.google.modernstorage.sample

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract

class ModernStorageExperiment(private val context: Context, private val registry: ActivityResultRegistry) {
    private lateinit var takePictureLauncher: ActivityResultLauncher<ActivityResultArguments<Uri, Uri?>>


    fun takePicture(filename: String, callback: (result: Uri?) -> Unit) {
        val resolver = context.contentResolver

        val photoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val newPhotoDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        }

        val uri = resolver.insert(photoCollection, newPhotoDetails)

        if (uri == null) {
            callback(null)
        } else {
            takePictureLauncher.launch(ActivityResultArguments(uri, callback))
        }
    }
}

data class ActivityResultArguments<I, O>(val value: I, val callback: (result: O) -> Unit)

class CustomTakePicture : ActivityResultContract<ActivityResultArguments<Uri, Uri?>, Unit>() {
    private lateinit var uri: Uri
    private lateinit var callback: (result: Uri?) -> Unit

    override fun createIntent(context: Context, input: ActivityResultArguments<Uri, Uri?>): Intent {
        uri = input.value
        callback = input.callback

        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri)
    }

    override fun parseResult(resultCode: Int, result: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return callback(null)
        }

        callback(uri)
    }
}
