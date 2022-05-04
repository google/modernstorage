/*
 * Copyright 2022 Google LLC
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
package com.google.modernstorage.photopicker

import com.google.modernstorage.photopicker.PickMedia.ImageAndVideo
import com.google.modernstorage.photopicker.PickMedia.MediaType

/**
 * Creates a request for a
 * [PickMultipleMedia] or
 * [PickMedia] Activity Contract.
 *
 * @param mediaType type to go into the PickMediaRequest
 *
 * @return a PickMediaRequest that contains the given input
 */
fun PickMediaRequest(
    mediaType: MediaType = ImageAndVideo
) = PickMediaRequest.Builder().setMediaType(mediaType).build()

/**
 * A request for a
 * [PickMultipleMedia] or
 * [PickMedia] Activity Contract.
 */
class PickMediaRequest internal constructor() {

    var mediaType: MediaType = ImageAndVideo
        internal set

    /**
     * A builder for constructing [PickMediaRequest] instances.
     */
    class Builder {

        private var mediaType: MediaType = ImageAndVideo

        /**
         * Set the media type for the [PickMediaRequest].
         *
         * The type is the mime type to filter by, e.g. `PickMedia.ImageOnly`,
         * `PickMedia.ImageAndVideo`, `PickMedia.SingleMimeType("image/gif")`
         *
         * @param mediaType type to go into the PickMediaRequest
         * @return This builder.
         */
        fun setMediaType(mediaType: MediaType): Builder {
            this.mediaType = mediaType
            return this
        }

        /**
         * Build the PickMediaRequest specified by this builder.
         *
         * @return the newly constructed PickMediaRequest.
         */
        fun build(): PickMediaRequest = PickMediaRequest().apply {
            this.mediaType = this@Builder.mediaType
        }
    }
}
