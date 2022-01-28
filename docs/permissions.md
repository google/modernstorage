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

## Check if app can access files

```kotlin
// Check if the app can read image & document files created by itself
val storagePermissions = StoragePermissions(context)

storagePermissions.hasAccess(
    action = Action.READ,
    types = listOf(FileType.Image, FileType.Document),
    createdBy = StoragePermissions.CreatedBy.Self
)

// Check if the app can read video & audio files created by all apps
storagePermissions.hasAccess(
    action = Action.READ,
    types = listOf(FileType.Video, FileType.Audio),
    createdBy = StoragePermissions.CreatedBy.AllApps
)

// Check if the app can read & write image & document files created by itself
val storagePermissions = StoragePermissions(context)

storagePermissions.hasAccess(
    action = Action.READ_AND_WRITE,
    types = listOf(FileType.Image, FileType.Video),
    createdBy = StoragePermissions.CreatedBy.Self
)

// Check if the app can read & write video & audio files created by all apps
storagePermissions.hasAccess(
    action = Action.READ_AND_WRITE,
    types = listOf(FileType.Audio, FileType.Document),
    createdBy = StoragePermissions.CreatedBy.AllApps
)
```

## Get storage permissions

!!! note ""

    If the method returns an empty list, it means your app on the current device, given the defined
    usage,  doesn't need any permissions.

```kotlin
// Get required permissions to read & write video & audio files created by all apps
storagePermissions.getPermissions(
    action = Action.READ,
    types = listOf(FileType.Video, FileType.Audio),
    createdBy = StoragePermissions.CreatedBy.AllApps
)

// Get required permissions to read & write image & document files created by the app itself
StoragePermissions.getPermissions(
    action = Action.READ_AND_WRITE,
    types = listOf(FileType.Image, FileType.Document),
    createdBy = StoragePermissions.CreatedBy.Self
)
```

## Request storage permissions

While you can use the `ActivityResultContracts.RequestPermission` provided by default with the
Jetpack Activity or Fragment library to request storage permissions with input from
`StoragePermissions.getPermissions`, `{{ artifact }}` bundles a custom ActivityResultContract named
`RequestAccess` to request the right storage permissions to simplify the logic for you.

=== "Compose"

    ```kotlin
    @Composable
    fun RequestAccessExample() {
        // Register a callback for the Activity Result
        val requestAccess = rememberLauncherForActivityResult(RequestAccess()) { hasAccess ->
            if (hasAccess) {
                // write logic here
            }
        }

        Column {
            Button(onClick = {
                // Request permission to read video & audio files created by all apps
                requestAccess.launch(
                     RequestAccess.Args(
                        action = Action.READ,
                        types = listOf(
                            StoragePermissions.FileType.Video,
                            StoragePermissions.FileType.Audio
                        )
                    ),
                    createdBy = StoragePermissions.CreatedBy.AllApps
                )
            }) {
                Text("I want to read all video & audio files")
            }

            Button(onClick = {
                // Request permission to read & write image & document files created by the app itself
                requestAccess.launch(
                     RequestAccess.Args(
                        action = Action.READ_AND_WRITE,
                        types = listOf(
                            StoragePermissions.FileType.Image,
                            StoragePermissions.FileType.Document
                        )
                    ),
                    createdBy = StoragePermissions.CreatedBy.Self
                )
            }) {
                Text("I want to read & write the app's image & document files")
            }
        }
    }
    ```

=== "Views"

    ```kotlin
    // Register a callback for the Activity Result
    val requestAccess = registerForActivityResult(RequestAccess()) { hasAccess ->
        if (hasAccess) {
            // write logic here
        }
    }


    // Request permission to read video & audio files created by all apps
    requestAccess.launch(
         RequestAccess.Args(
            action = Action.READ,
            types = listOf(StoragePermissions.FileType.Video, StoragePermissions.FileType.Audio),
            createdBy = StoragePermissions.CreatedBy.AllApps
        )
    )

    // Request permission to read & write image & document files created by the app itself
    requestAccess.launch(
         RequestAccess.Args(
            action = Action.READ_AND_WRITE,
            types = listOf(StoragePermissions.FileType.Image, StoragePermissions.FileType.Document),
            createdBy = StoragePermissions.CreatedBy.Self
        )
    )
    ```

[api_reference]: /modernstorage/api/permissions/
[saf_guide]: https://developer.android.com/training/data-storage/shared/documents-files
[edit_media_scoped_storage]: https://developer.android.com/training/data-storage/shared/media#update-other-apps-files
