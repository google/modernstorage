package com.google.modernstorage.sample_compose

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.modernstorage.storage.SharedFileSystem

class StorageViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context
        get() = getApplication()

    val filesystem = SharedFileSystem(context)
}
