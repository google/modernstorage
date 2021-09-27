/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.modernstorage.sample

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.modernstorage.mediastore.FileResource
import com.google.modernstorage.mediastore.FileType
import com.google.modernstorage.mediastore.MediaStoreRepository
import com.google.modernstorage.mediastore.SharedPrimary
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

const val TAG = "AppViewModel"
const val ADDED_MEDIA_KEY = "addedMedia"

class AppViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val context: Context
        get() = getApplication()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow

    private val mediaStore = MediaStoreRepository(context)
    private val permissionState = MutableStateFlow(StoragePermissionState.create(context))

    fun updatePermissionState() {
        permissionState.value = StoragePermissionState.create(context)
    }

    /**
     * We keep the current media [Uri] in the savedStateHandle to re-render it if there is a
     * configuration change and we expose it as a [LiveData] to the UI
     */
    val addedMedia: LiveData<FileResource?> =
        savedStateHandle.getLiveData<FileResource?>(ADDED_MEDIA_KEY)

    fun addImage() {
        viewModelScope.launch {
            context.assets.open("sample.jpg").use { inputStream ->
                val uri = mediaStore.addMediaFromStream(
                    filename = "added-${System.currentTimeMillis()}.jpg",
                    type = FileType.IMAGE,
                    mimeType = "image/jpg",
                    inputStream = inputStream,
                    location = SharedPrimary
                ).getOrElse {
                    Log.e(TAG, it.toString())
                    return@launch
                }

                savedStateHandle[ADDED_MEDIA_KEY] = mediaStore.getResourceByUri(uri)
            }
        }
    }
}
