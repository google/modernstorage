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

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.google.modernstorage.filesystem.internal.AndroidContentContract
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
     * Used to perform one-time initialization for the filesystem library.
     *
     * This method must be called before utilizing [AndroidFileSystems] or [AndroidPaths].
     */
    @JvmStatic
    fun initialize(context: Context) {
        initialize(AndroidContentContract(context))
    }

    /**
     * Convenience method to retrieve a [FileSystem] given an Android [Uri].
     */
    @JvmStatic
    fun getFileSystem(uri: Uri) = getFileSystem(uri.toURI())

    @JvmStatic
    fun getFileSystem(uri: URI): FileSystem {
        ensureProvidersLoaded()
        val provider = installedProviders[uri.scheme]
        return if (provider != null) {
            provider.getFileSystem(uri)
        } else {
            FileSystems.getFileSystem(uri)
        }
    }

    @JvmStatic
    fun newFileSystem(uri: URI, env: MutableMap<String, Any>): FileSystem {
        ensureProvidersLoaded()
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
            if (installedProviders[fileSystemProvider.scheme] != null) {
                throw IllegalStateException(
                    "Provider for ${fileSystemProvider.scheme} already registered"
                )
            }
            installedProviders[fileSystemProvider.scheme] = fileSystemProvider
        }
    }

    internal fun getFileSystemProvider(uri: URI): FileSystemProvider {
        ensureProvidersLoaded()
        return installedProviders[uri.scheme] ?: getSystemLoadedProvider(uri.scheme)
    }

    private fun ensureProvidersLoaded() {
        synchronized(installedProviders) {
            if (installedProviders.isEmpty() ||
                installedProviders[ContentResolver.SCHEME_CONTENT] == null
            ) {
                throw IllegalStateException(NOT_INITIALIZED)
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

    /**
     * Used to perform one-time initialization for the filesystem library with a custom
     * [PlatformContract]. Used for testing.
     */
    internal fun initialize(contract: PlatformContract) {
        synchronized(installedProviders) {
            installedProviders.remove(CONTENT_SCHEME)
        }
        installContentFileSystem(contract)
    }

    /**
     * Installs an implementation of a [ContentFileSystemProvider] backed by a platform specific
     * [PlatformContract].
     */
    private fun installContentFileSystem(contract: PlatformContract) {
        synchronized(installedProviders) {
            if (installedProviders[CONTENT_SCHEME] == null) {
                installProvider(ContentFileSystemProvider(contract))
            }
        }
    }
}

private const val NOT_INITIALIZED = "AndroidFileSystems.initialize was not called"
