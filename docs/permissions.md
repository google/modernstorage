---
artifact: "modernstorage-permissions"
---

# Permissions

`{{ artifact }}` is a library simplifying checking storage permissions. It works on Android 21+.

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
StoragePermissions.canReadFiles(
    types = listOf(FileType.Image, FileType.Document),
    createdBy = CreatedBy.Self
)

// Check if the app can read video & audio files created by all apps
StoragePermissions.canReadFiles(
    types = listOf(FileType.Video, FileType.Audio),
    createdBy = CreatedBy.AllApps
)
```

## Check if app can read and write files

```kotlin
// Check if the app can read & write image & document files created by itself
StoragePermissions.canReadAndWriteFiles(
    types = listOf(FileType.Image, FileType.Video),
    createdBy = CreatedBy.Self
)

// Check if the app can read & write video & audio files created by all apps
StoragePermissions.canReadAndWriteFiles(
    types = listOf(FileType.Audio, FileType.Document),
    createdBy = CreatedBy.AllApps
)
```

[api_reference]: /modernstorage/api/permissions/
