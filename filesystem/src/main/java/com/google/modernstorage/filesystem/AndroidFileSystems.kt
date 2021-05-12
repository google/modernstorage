/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.modernstorage.filesystem

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems
import java.nio.file.spi.FileSystemProvider

/**
 * Android wrapper around the [FileSystems] factory methods.
 *
 * This class handles the creation of a [ContentFileSystemProvider] as the default file system
 * provider for `content://` scheme Uris. It also supports registering alternative schemes
 * by calling [AndroidFileSystems.installProvider].
 *
 * This class automatically calls through to [FileSystems] if it cannot handle the request.
 */
@Suppress("unused")
object AndroidFileSystems {
    private val installedProviders = mutableMapOf<String, FileSystemProvider>()

    /**
     * Convenience method to retrieve a [FileSystem] given an Android [Uri].
     */
    @JvmStatic
    fun getFileSystem(context: Context, uri: Uri) = getFileSystem(context, uri.toURI())

    @JvmStatic
    fun getFileSystem(context: Context, uri: URI): FileSystem {
        ensureProvidersLoaded(context)
        val provider = installedProviders[uri.scheme]
        return if (provider != null) {
            provider.getFileSystem(uri)
        } else {
            FileSystems.getFileSystem(uri)
        }
    }

    @JvmStatic
    fun newFileSystem(context: Context, uri: URI, env: MutableMap<String, Any>): FileSystem {
        ensureProvidersLoaded(context)
        val provider = installedProviders[uri.scheme]
        return if (provider != null) {
            provider.newFileSystem(uri, env)
        } else {
            FileSystems.newFileSystem(uri, env)
        }
    }

    /**
     * Installs a new [FileSystemProvider].
     * Throw [IllegalStateException] if the `scheme`, as reported by
     * [FileSystemProvider.getScheme], has already been registered.
     */
    @JvmStatic
    @Throws(IllegalStateException::class)
    fun installProvider(fileSystemProvider: FileSystemProvider) {
        synchronized(installedProviders) {
            if (fileSystemProvider.scheme == ContentResolver.SCHEME_CONTENT ||
                fileSystemProvider.scheme in installedProviders
            ) {
                throw IllegalStateException(
                    "Provider for ${fileSystemProvider.scheme} already registered"
                )
            }
            installedProviders[fileSystemProvider.scheme] = fileSystemProvider
        }
    }

    internal fun getFileSystemProvider(context: Context, uri: URI): FileSystemProvider {
        ensureProvidersLoaded(context)
        return installedProviders[uri.scheme] ?: getSystemLoadedProvider(uri.scheme)
    }

    private fun ensureProvidersLoaded(context: Context) {
        synchronized(installedProviders) {
            if (installedProviders.isEmpty() ||
                !installedProviders.containsKey(ContentResolver.SCHEME_CONTENT)
            ) {
                val contentFileSystemProvider =
                    ContentFileSystemProvider(context.applicationContext)
                installedProviders[contentFileSystemProvider.scheme] = contentFileSystemProvider
            }
        }
    }

    private fun getSystemLoadedProvider(scheme: String): FileSystemProvider {
        FileSystemProvider.installedProviders().forEach { installedProvider ->
            if (installedProvider.scheme == scheme) {
                return installedProvider
            }
        }
        throw FileSystemNotFoundException("Could not find file system for scheme: $scheme")
    }
}
