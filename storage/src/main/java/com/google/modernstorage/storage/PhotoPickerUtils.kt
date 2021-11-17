package com.google.modernstorage.storage

import android.annotation.SuppressLint
import android.provider.MediaStore
import androidx.core.os.BuildCompat

@SuppressLint("UnsafeOptInUsageError")
fun isPhotoPickerAvailablePreview(): Boolean {
    return if(BuildCompat.isAtLeastT()) {
        true
    } else {
        try {
            MediaStore::class.java.getDeclaredField("ACTION_PICK_IMAGES")
            true
        } catch (e: NoSuchFieldException) {
            false
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
fun isPhotoPickerAvailable(): Boolean {
    return try {
        MediaStore::class.java.getDeclaredField("ACTION_PICK_IMAGES")
        true
    } catch (e: NoSuchFieldException) {
        false
    }
}
