package com.google.modernstorage.media

sealed class StorageLocation
object Internal : StorageLocation()
object SharedPrimary : StorageLocation()
// TODO: Handle secondary shared storage
//class SharedSecondary(val volume: StorageVolume) : StorageLocation()