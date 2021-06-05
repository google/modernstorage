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
package com.google.modernstorage.mediastore

import android.net.Uri
import android.provider.MediaStore.Files.FileColumns

/**
 * Represents an [android.provider.MediaStore] entry.
 *
 * @property uri The first name.
 * @property filename File name with extension.
 * @property size Size of the file in bytes.
 * @property type The last name.
 * @property mimeType The last name.
 */
data class MediaResource(
    val uri: Uri,
    val filename: String,
    val size: Long,
    val type: MediaType,
    val mimeType: String,
)

/**
 *  Media type enum class representing the [FileColumns.MEDIA_TYPE] column
 */
enum class MediaType(val value: Int) {
    /**
     * Representing [FileColumns.MEDIA_TYPE_NONE]
     */
    NONE(0),

    /**
     * Representing [FileColumns.MEDIA_TYPE_IMAGE]
     */
    IMAGE(1),

    /**
     * Representing [FileColumns.MEDIA_TYPE_AUDIO]
     */
    AUDIO(2),

    /**
     * Representing [FileColumns.MEDIA_TYPE_VIDEO]
     */
    VIDEO(3),

    /**
     * Representing [FileColumns.MEDIA_TYPE_PLAYLIST]
     */
    PLAYLIST(4),

    /**
     * Representing [FileColumns.MEDIA_TYPE_SUBTITLE]
     */
    SUBTITLE(5),

    /**
     * Representing [FileColumns.MEDIA_TYPE_DOCUMENT]
     */
    DOCUMENT(6);

    companion object {
        /**
         * Returns the matching [MediaType] enum given an int value
         *
         * @param value int value of the [MediaType] as written in [FileColumns.MEDIA_TYPE] column
         */
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
