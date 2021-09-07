# MediaStore

On Android, every files on the shared storage (files visible when using a file manager) are indexed 
by [MediaStore][mediastore_api], to allow apps to query them by type, date, size, etc.

On API 29 (Android 10), Scoped Storage has been introduced, enabling a sandboxed view on the shared 
storage and the ability to add files without requesting permission. To read media files created by 
other apps, you need to request `REQUEST_EXTERNAL_STORAGE` and editing/deleting them require to ask
[user's consent][manage_3rd_party_media_files].

`modernstorage-mediastore` is a library abstracting these [MediaStore][mediastore_api] API 
interactions on API 21+ (Android Lollipop).

## Add dependency to project

`modernstorage-mediastore` is available on `mavenCentral()`.

```groovy
// build.gradle
implementation("com.google.modernstorage:modernstorage-mediastore:{{ lib_version }}")
```

## API reference
`modernstorage-mediastore` API reference is available [here][api_reference].

## Initialize repository
To interact with MediaStore, you need to initialize a [MediaStoreRepository][mediastore_repository] 
instance:

```kotlin
val mediaStore by lazy { MediaStoreRepository(context) }
```

## Checking permissions
Before adding, editing or deleting files, you should check if you have the permissions to do so.

To avoid having a complex permission checking logic due to Scoped Storage changes, we have included 
helper methods in [MediaStoreRepository][mediastore_repository]:

```kotlin
/**
 * If you only need to read your own entries in MediaStore
 */
if(mediaStore.canReadOwnEntries()) {
    println("Read my image")
}

/**
 * If you only need to read and write your own entries in MediaStore
 */
if(mediaStore.canWriteOwnEntries()) {
    println("Edit my image")
}

/**
 * If you need to read your own entries as well as the ones created by other apps in MediaStore
 */
if(mediaStore.canReadSharedEntries()) {
    println("Read an image created by another app")
}

/**
 * If you need to read and write your own entries as well as the ones created by other apps in 
 * MediaStore
 */
if(mediaStore.canWriteSharedEntries()) {
    println("Edit an image created by another app")
}
```

## Create media URI
If you're using intents like [ACTION_IMAGE_CAPTURE][intent_action_image_capture] and want to 
personalize the MediaStore entry, use the `createMediaUri` method:

```kotlin
val photoUri = mediaStore.createMediaUri(
    filename = "new-image.jpg",
    type = FileType.IMAGE,
    location = SharedPrimary
).getOrElse { reason ->
    println("Creating Media URI failed: $reason")
}
```

## Add media file
You can add a media file by using the method `addMediaFromStream`. It will create a MediaStore URI, 
save the `inputStream` content in it, and scans the file before returning its Uri:

```kotlin
/**
 * If you already have an InputStream
 */
val photoUri = mediaStore.addMediaFromStream(
   filename = "new-image.jpg",
   type = FileType.IMAGE,
   mimeType = "image/jpg",
   inputStream = sample,
   location = SharedPrimary
).getOrElse { reason ->
    println("Creating Media URI failed: $reason")
}

/**
 * Otherwise with a ByteArray
 */
val videoUri = mediaStore.addMediaFromStream(
   filename = "new-video.mp4",
   type = FileType.VIDEO,
   mimeType = "video/mp4",
   inputStream = ByteArrayInputStream(sampleByteArray),
   location = SharedPrimary
).getOrElse { reason ->
    println("Creating Media URI failed: $reason")
}
```

## Scan media URI
Modifying a file requires to scan it to make MediaStore aware of the file changes (size, 
modification date, etc.). To request a scan for a media URI, use the `scanUri` method:

```kotlin
mediaStore.scanUri(updatedPhotoUri, "image/png").getOrElse { reason ->
    println("Scanning failed ($mediaUri): $reason")
}
```

## Get media URI details
To get the details of a media URI (filename, size, file type, mime type), use the `getResourceByUri`
method:

```kotlin
val mediaDetails = mediaStore.getResourceByUri(mediaUri).getOrElse { reason ->
    println("Fetching details failed ($mediaUri): $reason")
}
```


[mediastore_api]: https://developer.android.com/reference/kotlin/android/provider/MediaStore
[manage_3rd_party_media_files]: https://developer.android.com/training/data-storage/use-cases#modify-delete-media
[api_reference]: /modernstorage/api/mediastore
[mediastore_repository]: /modernstorage/api/mediastore/
[intent_action_image_capture]: https://developer.android.com/reference/kotlin/android/provider/MediaStore#action_image_capture
