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
package com.google.modernstorage.filesystem

import android.net.Uri
import com.google.modernstorage.filesystem.AndroidFileSystems.getFileSystemProvider
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Convenience methods for constructing a [Path] on Android.
 * These methods support creating a `Path` from `file:///`,
 * `content://`, and any other registered schemes.
 *
 * This class automatically handles integration with the [ContentFileSystemProvider],
 * and delegates calls for schemes other than `content://` to [java.nio.file.Paths].
 */
@Suppress("unused")
object AndroidPaths {
    /**
     * Convenience method to retrieve a [Path] for a given Android [Uri].
     * @see [Paths.get]
     */
    @JvmStatic
    fun get(uri: Uri): Path = get(uri.toURI())

    /**
     * Method to retrieve a [Path] for a given [URI]. This method will automatically fallback to
     * [Paths.get] if the `URI`s scheme doesn't match one registered with [AndroidFileSystems].

     * @see [Paths.get]
     */
    @JvmStatic
    fun get(uri: URI): Path = getFileSystemProvider(uri).getPath(uri)
}
