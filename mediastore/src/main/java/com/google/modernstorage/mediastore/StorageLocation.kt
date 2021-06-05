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


/**
 *  Represents a storage volume to be used as target when using [MediaStoreClient].
 */
sealed class StorageLocation

/**
 *  Represents the internal storage of the current application.
 */
object Internal : StorageLocation()

/**
 *  Represents the primary shared storage of the device.
 */
object SharedPrimary : StorageLocation()

// TODO: Handle secondary shared storage
// class SharedSecondary(val volume: StorageVolume) : StorageLocation()
