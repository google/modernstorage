package com.google.modernstorage.mediastore

//override fun getThumbnail(context: Context): Bitmap? {
//    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//        context.contentResolver.loadThumbnail(uri, MINI_KIND, null)
//    } else {
//        MediaStore.Video.Thumbnails.getThumbnail(
//            context.contentResolver,
//            uri.lastPathSegment!!.toLong(),
//            MediaStore.Video.Thumbnails.MINI_KIND,
//            null
//        )
//    }
//}


//override fun getThumbnail(context: Context): Bitmap? {
//    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//        context.contentResolver.loadThumbnail(uri, MINI_KIND, null)
//    } else {
//        MediaStore.Images.Thumbnails.getThumbnail(
//            context.contentResolver,
//            uri.lastPathSegment!!.toLong(),
//            MediaStore.Images.Thumbnails.MINI_KIND,
//            null
//        )
//    }
//}