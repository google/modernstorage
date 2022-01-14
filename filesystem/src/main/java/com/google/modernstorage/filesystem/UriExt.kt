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
import java.net.URI

/**
 * Convenience method to convert a [Uri] into a [URI].
 */
@Deprecated("Use the new storage module instead, this module will be removed at the next version")
fun Uri.toURI() = URI(this.toString())

/**
 * Convenience method to convert a [URI] into a [Uri].
 */
@Deprecated("Use the new storage module instead, this module will be removed at the next version")
fun URI.toUri(): Uri = Uri.parse(this.toString())