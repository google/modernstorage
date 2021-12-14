---
artifact: "modernstorage-permissions"
---

# FileSystem

`{{ artifact }}` is a library simplifying checking and requesting storage permissions on Android 21+.

## Add dependency to project

`{{ artifact }}` is available on `mavenCentral()`.

```groovy
// build.gradle
implementation("com.google.modernstorage:{{ artifact }}:{{ lib_version }}")
```

## API reference
`{{ artifact }}` API reference is available [here][api_reference].

## Check if app can read files

```kotlin
// Check if the app can read image & document files created by itself
StoragePermissions.canReadFiles(listOf(FileType.Image, FileType.Document), CreatedBy.Self)

// Check if the app can read video & audio files created by all apps
StoragePermissions.canReadFiles(listOf(FileType.Video, FileType.Audio), CreatedBy.AllApps)
```

## Check if app can read and write files

```kotlin
// Check if the app can read & write image & document files created by itself
StoragePermissions.canReadAndWriteFiles(listOf(FileType.Image, FileType.Video), CreatedBy.Self)

// Check if the app can read & write video & audio files created by all apps
StoragePermissions.canReadAndWriteFiles(listOf(FileType.Audio, FileType.Document), CreatedBy.AllApps)
```

[api_reference]: /modernstorage/api/permissions/
