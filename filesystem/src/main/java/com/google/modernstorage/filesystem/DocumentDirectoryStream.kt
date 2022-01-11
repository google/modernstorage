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

import java.nio.file.DirectoryStream
import java.nio.file.Path

@Deprecated("Use the new storage module instead, this module will be removed at the next version")
internal class SequenceDocumentDirectoryStream(
    private val sequence: Sequence<Path>,
) : DirectoryStream<Path> {
    private var receivedIterator = false

    private val streamIterator = object : MutableIterator<Path> {
        private val iterator = sequence.iterator()

        override fun hasNext() = iterator.hasNext()

        override fun next() = iterator.next()

        override fun remove() = throw UnsupportedOperationException()
    }

    override fun close() {
        // Nothing to do.
    }

    override fun iterator(): MutableIterator<Path> {
        if (receivedIterator) {
            throw IllegalStateException("Iterator already obtained")
        }
        receivedIterator = true
        return streamIterator
    }
}
