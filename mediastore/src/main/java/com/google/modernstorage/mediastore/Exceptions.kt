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
import android.os.RemoteException
import java.io.FileNotFoundException
import java.io.IOException

class UriNotCreatedException(message: String) : RemoteException(message)
class UnopenableOutputStreamException(message: String) : IOException(message)
class UriNotScannedException(message: String) : RemoteException(message)

internal object Exceptions {
    fun uriNotFoundException(uri: Uri) = FileNotFoundException("Uri $uri could not be found")

    fun uriNotCreatedException(displayName: String) =
        UriNotCreatedException("URI of the entry $displayName not be created")

    fun unopenableOutputStreamException(uri: Uri) =
        UnopenableOutputStreamException("OutputStream of $uri could not be opened")

    fun uriNotScannedException(uri: Uri) = UriNotScannedException("Uri $uri could not be scanned")
}
