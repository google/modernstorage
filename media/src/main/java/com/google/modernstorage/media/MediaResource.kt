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