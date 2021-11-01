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
