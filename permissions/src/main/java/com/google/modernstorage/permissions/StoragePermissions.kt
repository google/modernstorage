package com.google.modernstorage.permissions

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.BuildCompat

@BuildCompat.PrereleaseSdkCheck
class StoragePermissions(private val context: Context) {
    companion object {
        private const val VISUAL_PERMISSION = "READ_MEDIA_IMAGES_VIDEO"
        private const val AUDIO_PERMISSION = "READ_MEDIA_AUDIO"
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkFullStoragePermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    /**
     * Check if app can read its own visual media files (images & video)
     */
    fun canReadOwnVisualMedia(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            checkPermission(READ_EXTERNAL_STORAGE) || checkPermission(WRITE_EXTERNAL_STORAGE)
        }
    }
    fun requestReadOwnVisualMediaPermission(): ActivityResult = TODO("Have to do it")

    /**
     * Check if app can read its own audio media files
     */
    fun canReadOwnAudioMedia(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            checkPermission(READ_EXTERNAL_STORAGE) || checkPermission(WRITE_EXTERNAL_STORAGE)
        }
    }
    fun requestReadOwnAudioMediaPermission(): ActivityResult = TODO("Have to do it")

    /**
     * Check if app can read shared visual media files (images & video)
     */
    fun canReadSharedVisualMedia(): Boolean {
        return when {
            BuildCompat.isAtLeastT() -> {
                checkFullStoragePermission() || checkPermission(VISUAL_PERMISSION)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                checkFullStoragePermission() || checkPermission(READ_EXTERNAL_STORAGE) || checkPermission(WRITE_EXTERNAL_STORAGE)
            }
            else -> {
                checkPermission(READ_EXTERNAL_STORAGE) || checkPermission(WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    fun requestReadSharedVisualMediaPermission(): ActivityResult = TODO("Have to do it")











    /**
     * Check if app can read shared audio media files
     */
    fun canReadSharedAudioMedia(): Boolean {
        return when {
            BuildCompat.isAtLeastT() -> {
                checkFullStoragePermission() || checkPermission(AUDIO_PERMISSION)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                checkFullStoragePermission() || checkPermission(READ_EXTERNAL_STORAGE)
            }
            else -> {
                checkPermission(READ_EXTERNAL_STORAGE) || checkPermission(WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    fun requestReadSharedAudioMediaPermission(): ActivityResult = TODO("Have to do it")

    /**
     * Check if app can write its own visual media files (images & video)
     */
    fun canWriteOwnVisualMedia(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            checkPermission(WRITE_EXTERNAL_STORAGE)
        }
    }
    fun requestWriteOwnVisualMediaPermission(): ActivityResult = TODO("Have to do it")

    /**
     * Check if app can write its own audio media files
     */
    fun canWriteOwnAudioMedia(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            checkPermission(WRITE_EXTERNAL_STORAGE)
        }
    }
    fun requestWriteOwnAudioMediaPermission(): ActivityResult = TODO("Have to do it")

    /**
     * Check if app can write shared visual media files (images & video)
     */
    fun canWriteSharedVisualMedia(): Boolean {
        return when {
            BuildCompat.isAtLeastT() -> {
                checkFullStoragePermission() || checkPermission(AUDIO_PERMISSION)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                checkFullStoragePermission() || checkPermission(READ_EXTERNAL_STORAGE)
            }
            else -> {
                checkPermission(WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    fun requestWriteSharedVisualMediaPermission(): ActivityResult = TODO("Have to do it")

    /**
     * Check if app can write shared audio media files
     */
    fun canWriteSharedAudioMedia(): Boolean {
        return when {
            BuildCompat.isAtLeastT() -> {
                checkFullStoragePermission() || checkPermission(AUDIO_PERMISSION)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                checkFullStoragePermission() || checkPermission(READ_EXTERNAL_STORAGE)
            }
            else -> {
                checkPermission(WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    fun requestWriteSharedAudioMediaPermission(): ActivityResult = TODO("Have to do it")
}
