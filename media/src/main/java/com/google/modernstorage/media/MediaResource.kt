package com.google.modernstorage.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size

interface MediaResource {
    val uri: Uri
    val filename: String
    val size: Long

    fun getThumbnail(context: Context): Bitmap?
}

private val MINI_KIND = Size(512, 384)

data class ImageResource(
    override val uri: Uri,
    override val filename: String,
    override val size: Long
) : MediaResource {

    override fun getThumbnail(context: Context): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(uri, MINI_KIND, null)
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(
                context.contentResolver,
                uri.lastPathSegment!!.toLong(),
                MediaStore.Images.Thumbnails.MINI_KIND,
                null
            )
        }
    }
}

data class VideoResource(
    override val uri: Uri,
    override val filename: String,
    override val size: Long
) : MediaResource {

    override fun getThumbnail(context: Context): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(uri, MINI_KIND, null)
        } else {
            MediaStore.Video.Thumbnails.getThumbnail(
                context.contentResolver,
                uri.lastPathSegment!!.toLong(),
                MediaStore.Video.Thumbnails.MINI_KIND,
                null
            )
        }
    }
}
