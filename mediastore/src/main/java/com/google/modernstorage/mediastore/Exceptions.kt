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

internal object Exceptions {
    class UriNotFoundException(uri: Uri) : FileNotFoundException() {
        override val message = "Uri $uri could not be found"
    }

    class UriNotCreatedException(displayName: String) : RemoteException() {
        override val message = "URI of the entry $displayName not be created"
    }

    class UnopenableOutputStreamException(uri: Uri) : IOException() {
        override val message = "Uri $uri is not a Media Uri"
    }
    class UriNotScannedException(uri: Uri) : RemoteException() {
        override val message = "Uri $uri could not be scanned"
    }

    class FileNotScannedException(path: String) : RemoteException() {
        override val message = "File $path could not be scanned"
    }

    class UnsupportedMediaUriException(uri: Uri) : IllegalArgumentException() {
        override val message = "Uri $uri is not a Media Uri"
    }
}
