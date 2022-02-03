---
artifact: "modernstorage-storage"
---

# Storage Interactions

`{{ artifact }}` is a library abstracting storage interactions on Android using the library
[Okio][okio_website]. It relies on its [FileSystem][okio_filesystem_guide] API, which provides a
[set of methods][okio_filesystem_api] to read and write files.

Instead of opening an `InputStream` or `OutputStream` and relies on different APIs to get file
metadata for MediaStore and Storage Access Framework `DocumentProvider`, this library takes
[Uri][uri_api] (changed to [Path][okio_path_api] to make it work with Okio) as an input for all its
methods to read and write files but also get metadata.

## Add dependency to project

`{{ artifact }}` is available on `mavenCentral()`.

```groovy
// build.gradle

// {{ artifact }} requires Okio 3.x.x as a dependency
implementation("com.squareup.okio:okio:3.0.0")
implementation("com.google.modernstorage:{{ artifact }}:{{ lib_version }}")
```

## API reference
`{{ artifact }}` API reference is available [here][api_reference].

## Initialize before usage
To interact with the [FileSystem][okio_filesystem_guide] API, you need to initialize an instance
first:

```kotlin
import com.google.modernstorage.storage.AndroidFileSystem

val fileSystem = AndroidFileSystem(context)
```

## Get Path from Uri
Call `toPath` to get a `Path` from a `Uri`:

```kotlin
val path = uri.toPath()
```

## Get file metadata
You can get the file size by using the method `metadataOrNull`:

```kotlin
import com.google.modernstorage.storage.MetadataExtras.DisplayName
import com.google.modernstorage.storage.MetadataExtras.MimeType

val fileMetadata = fileSystem.metadataOrNull(uri.toPath())
Log.d("ModernStorage/uri", uri.toString())
Log.d("ModernStorage/isRegularFile", metadata.isRegularFile.toString())
Log.d("ModernStorage/isDirectory", metadata.isDirectory.toString())
Log.d("ModernStorage/size", metadata.size.toString())
Log.d("ModernStorage/lastModifiedAtMillis", metadata.lastModifiedAtMillis.toString())
Log.d("ModernStorage/displayName", metadata.extra(DisplayName::class).value)
Log.d("ModernStorage/mimeType", metadata.extra(MimeType::class).value)
```

## Read a Text file Uri from the Storage Access Framework
```kotlin
/**
 * We register first an ActivityResult handler for Intent.ACTION_OPEN_DOCUMENT
 * Read more about ActivityResult here: https://developer.android.com/training/basics/intents/result
 */
val actionOpenTextFile = registerForActivityResult(OpenDocument()) { uri ->
    if(uri != null) {
        // textPath is an instance of okio.Path
        val textPath = uri.toPath()
        Log.d("ModernStorage/metadata", fileSystem.metadataOrNull(textPath).toString())
        Log.d("ModernStorage/content", fileSystem.source(textPath).buffer().readUtf8())
    }
}

// Open file picker
actionOpenTextFile.launch(arrayOf("text/*"))
```

[api_reference]: /modernstorage/api/storage/
[okio_website]: https://square.github.io/okio/
[okio_filesystem_guide]: https://square.github.io/okio/file_system/
[okio_filesystem_api]: https://square.github.io/okio/3.x/okio/okio/okio/-file-system/index.html
[uri_api]: https://developer.android.com/reference/kotlin/android/net/Uri
[okio_path_api]: https://square.github.io/okio/3.x/okio/okio/okio/-path/index.html
