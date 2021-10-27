package com.google.modernstorage.storage

import android.net.Uri
import okio.Path
import okio.Path.Companion.toPath

fun Path.toUri(): Uri {
    // TODO: Add proper verification
    // TODO: Check if URI authority is compatible with our API
    return Uri.parse(this.toString())
}

fun Uri.toPath(): Path {
    // TODO: Add proper verification
    // TODO: Check if URI authority is compatible with our API
    return this.toString().toPath()
}
