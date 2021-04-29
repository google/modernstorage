package com.google.modernstorage.media

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi

fun getImageCollection(location: StorageLocation): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getImageCollectionApi29(location)
    } else {
        getImageCollectionApi21(location)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun getImageCollectionApi29(location: StorageLocation): Uri? {
    return when(location) {
        Internal -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_INTERNAL)
        SharedPrimary -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }
}

private fun getImageCollectionApi21(location: StorageLocation): Uri? {
    return when(location) {
        Internal -> MediaStore.Images.Media.INTERNAL_CONTENT_URI
        SharedPrimary -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
}



fun getVideoCollection(location: StorageLocation): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getVideoCollectionApi29(location)
    } else {
        getVideoCollectionApi21(location)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun getVideoCollectionApi29(location: StorageLocation): Uri? {
    return when(location) {
        Internal -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_INTERNAL)
        SharedPrimary -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }
}

private fun getVideoCollectionApi21(location: StorageLocation): Uri? {
    return when(location) {
        Internal -> MediaStore.Video.Media.INTERNAL_CONTENT_URI
        SharedPrimary -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }
}