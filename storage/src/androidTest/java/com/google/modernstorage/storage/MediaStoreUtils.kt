package com.google.modernstorage.storage

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object MediaStoreUtils {

    suspend fun createImageUri(extension: String, prefix: String = "") {
        val filename = prefix + System.currentTimeMillis().toString() + "." + extension

    }

    fun createImageUri(context: Context, filename: String): Uri? {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val newImage = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        }

        return context.contentResolver.insert(imageCollection, newImage)
    }

    fun addJpegImage(context: Context): Uri {
        val filename = "added-${System.currentTimeMillis()}.jpg"
        val imageUri = createImageUri(context, filename)

        checkNotNull(imageUri) {
            "Image URI couldn't be created"
        }

        context.contentResolver.openOutputStream(imageUri).use { outputStream ->
            checkNotNull(outputStream) {
                "Output stream couldn't be opened"
            }

            context.assets.open("sample.jpg").use { inputStream ->
                inputStream.copyTo(outputStream)
            }

            return imageUri
        }
    }

    suspend fun scanUri(context: Context, uri: Uri, mimeType: String): Uri? {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(MediaStore.Files.FileColumns.DATA),
            null,
            null,
            null
        ) ?: throw Exception("Uri $uri could not be found")

        val path = cursor.use {
            if (!cursor.moveToFirst()) {
                throw Exception("Uri $uri could not be found")
            }

            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
        }

        return suspendCancellableCoroutine { continuation ->
            MediaScannerConnection.scanFile(
                context,
                arrayOf(path),
                arrayOf(mimeType)
            ) { _, scannedUri ->
                if (scannedUri == null) {
                    continuation.cancel(Exception("File $path could not be scanned"))
                } else {
                    continuation.resume(scannedUri)
                }
            }
        }
    }
}
