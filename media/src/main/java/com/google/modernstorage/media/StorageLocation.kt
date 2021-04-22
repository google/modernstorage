package com.google.modernstorage.media

import android.graphics.Bitmap
import android.net.Uri
import android.os.storage.StorageVolume
import java.io.OutputStream

sealed class StorageLocation
object Internal : StorageLocation()
object SharedPrimary : StorageLocation()
//class SharedSecondary(val volume: StorageVolume) : StorageLocation()

interface ModernStorageInterface {
    fun createUri(filename: String, collection: Uri)
    suspend fun addImage(
        filename: String,
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat,
        quality: Int = 100,
        location: StorageLocation
    )

    suspend fun addImage(
        filename: String,
        bytes: ByteArray,
        compressFormat: Bitmap.CompressFormat,
        quality: Int = 100,
        location: StorageLocation
    )

    suspend fun addImage(
        filename: String,
        stream: OutputStream,
        compressFormat: Bitmap.CompressFormat,
        quality: Int = 100,
        location: StorageLocation
    )

    suspend fun modifyImage(
        uri: Uri,
        data: ByteArray,
        compressFormat: Bitmap.CompressFormat,
        quality: Int = 100,
    )
}

class ModernStorage
