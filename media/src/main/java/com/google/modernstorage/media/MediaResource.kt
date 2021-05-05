/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.modernstorage.media

import android.net.Uri

data class MediaResource(
    val uri: Uri,
    val filename: String,
    val size: Long,
    val type: MediaType,
    val mimeType: String,
)

enum class MediaType(val value: Int) {
    NONE(0),
    IMAGE(1),
    AUDIO(2),
    VIDEO(3),
    PLAYLIST(4),
    SUBTITLE(5),
    DOCUMENT(6);

    companion object {
        fun getEnum(value: Int): MediaType {
            return when (value) {
                0 -> NONE
                1 -> IMAGE
                2 -> AUDIO
                3 -> VIDEO
                4 -> PLAYLIST
                5 -> SUBTITLE
                6 -> DOCUMENT
                else -> throw Exception("Unknown MediaStoreType value")
            }
        }
    }
}