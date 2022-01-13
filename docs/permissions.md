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

## Storage Permissions usage

Android provides two types of storage to save files:

* App-specific (internal folder, external app folder)
* Shared (visible using the system file manager)

With the introduction of Scoped Storage in Android 10 (API 29), the storage access has deeply
changed:

* You can add media & document files without any permission, reading and editing media files
created by other apps require `READ_EXTERNAL_STORAGE`
* Document files created by other apps are readable only using the [Storage Access Framework][saf_guide]

To help you navigate common use cases, check out the below table:

| Actions                                            | API 29-                  | API 29+                     |
|----------------------------------------------------|--------------------------|-----------------------------|
| Read media files (created by self)                 | `READ_EXTERNAL_STORAGE`  | No permission needed        |
| Read media files (created by all apps)             | `READ_EXTERNAL_STORAGE`  | `READ_EXTERNAL_STORAGE`     |
| Add file (media, document)                         | `WRITE_EXTERNAL_STORAGE` | No permission needed        |
| Edit & Delete media files (created by self)        | `WRITE_EXTERNAL_STORAGE` | No permission needed        |
| Edit & Delete media files (created by all apps)    | `WRITE_EXTERNAL_STORAGE` | `READ_EXTERNAL_STORAGE` 1️⃣  |
| Edit & Delete document files (created by self)     | `WRITE_EXTERNAL_STORAGE` | No permission needed        |
| Edit & Delete document files (created by all apps) | `WRITE_EXTERNAL_STORAGE` | Storage Access Framework    |

> 1️⃣ When editing or deleting media files created by other apps on API 29+ (Android 10), you have to
> request explicitly user's consent. Read more [here][edit_media_scoped_storage].

## Check if app can read files

```kotlin
// Check if the app can read image & document files created by itself
val storagePermissions = StoragePermissions(context)

storagePermissions.canReadFiles(
    types = listOf(FileType.Image, FileType.Document),
    createdBy = StoragePermissions.CreatedBy.Self
)

// Check if the app can read video & audio files created by all apps
storagePermissions.canReadFiles(
    types = listOf(FileType.Video, FileType.Audio),
    createdBy = StoragePermissions.CreatedBy.AllApps
)
```

## Check if app can read and write files

```kotlin
// Check if the app can read & write image & document files created by itself
val storagePermissions = StoragePermissions(context)

storagePermissions.canReadAndWriteFiles(
    types = listOf(FileType.Image, FileType.Video),
    createdBy = StoragePermissions.CreatedBy.Self
)

// Check if the app can read & write video & audio files created by all apps
storagePermissions.canReadAndWriteFiles(
    types = listOf(FileType.Audio, FileType.Document),
    createdBy = StoragePermissions.CreatedBy.AllApps
)
```

[api_reference]: /modernstorage/api/permissions/
[saf_guide]: https://developer.android.com/training/data-storage/shared/documents-files
[edit_media_scoped_storage]: https://developer.android.com/training/data-storage/shared/media#update-other-apps-files
