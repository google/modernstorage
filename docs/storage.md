---
artifact: "modernstorage-storage"
---

# Storage Interactions

`{{ artifact }}` is a library abstracting interactions on the Android shared storage using the
library [Okio][okio_website]. It relies on its [FileSystem][okio_filesystem_guide] API, which
provides a [set of methods][okio_filesystem_api] to read and write files.

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
import com.google.modernstorage.storage.SharedFileSystem

val fileSystem = SharedFileSystem(context)
```

## Read a Uri returned by the Storage Access Framework
```kotlin
/**
 * We register first an ActivityResult handler for Intent.ACTION_OPEN_DOCUMENT
 * Read more about ActivityResult here: https://developer.android.com/training/basics/intents/result
 */
val actionOpenTextFile = registerForActivityResult(OpenDocument()) { uri ->
    if(uri != null) {
        // textPath is an instance of okio.Path
        val textPath = uri.toPath()
        Log.d("ModernStorage/metadata", fileSystem.metadataOrNull(textPath))
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
