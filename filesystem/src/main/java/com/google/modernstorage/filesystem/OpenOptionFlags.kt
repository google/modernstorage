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

import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.WRITE

/**
 * Representation of [StandardOpenOption]s as flags.
 */
class OpenOptionFlags(options: Set<OpenOption>?) {
    val read: Boolean
    val write: Boolean
    val append: Boolean
    val create: Boolean
    val exclusive: Boolean
    val truncate: Boolean

    init {
        val opts = options ?: setOf(READ)
        read = opts.contains(READ)
        write = opts.contains(WRITE)
        append = opts.contains(APPEND)
        create = opts.contains(CREATE) || opts.contains(CREATE_NEW)
        exclusive = opts.contains(CREATE_NEW)
        truncate = opts.contains(TRUNCATE_EXISTING)

        // Validity check
        if (read && append) throw IllegalArgumentException("Cannot open for read + append")
        if (append && truncate) throw IllegalArgumentException("Cannot open for append + truncate")
    }

    /**
     * Converts the flags to a mode string suitable to be used to open an
     * [android.os.ParcelFileDescriptor].
     */
    fun toMode(): String {
        var modeString = ""

        if (read) modeString += "r"
        if (write) modeString += "w"
        if (truncate) modeString += "t"
        if (append) modeString += "a"

        /*
         * The OpenJDK will pass the options WRITE + TRUNCATE_EXISTING together, but Android's
         * ParcelFileDescriptor doesn't handle "wt" as a valid mode, so we need to change it
         * to also include reading, which is then supported again.
         */
        if (!read && write && truncate) modeString = "r$modeString"

        return if (modeString.isEmpty()) "r" else modeString
    }
}
