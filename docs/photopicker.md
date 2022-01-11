---
artifact: "modernstorage-photopicker"
---

# Photo Picker

`{{ artifact }}` is a library providing an `ActivityResultContract` for the
[ActivityResult][activity_result_api] API to launch the Photo Picker intent when available on device
or rely on the existing system file picker using the `ACTION_OPEN_DOCUMENT` intent. It works on Android 21+.

!!! warning
    The Photo Picker feature is part of the next Android version as mentioned
    [here][ads_photo_picker]. There isn't a developer release yet, stay tuned for more updates.

## Add dependency to project

`{{ artifact }}` is available on `mavenCentral()`.

```groovy
// build.gradle
implementation("com.google.modernstorage:{{ artifact }}:{{ lib_version }}")
```

## API reference
`{{ artifact }}` API reference is available [here][api_reference].

## How to use the photo picker

=== "Compose"

    ```kotlin
    @Composable
    fun PhotoPickerExample() {
        // Register a callback for the Activity Result
        val photoPicker = rememberLauncherForActivityResult(PhotoPicker()) { uris ->
            // uris contain the list of selected images & video
            println(uris)
        }

        Column {
            Button(onClick = {
                // Launch the picker with only one image selectable
                photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_ONLY, 1))
            }) {
                Text("Select 1 image max")
            }

            Button(onClick = {
                // Launch the picker with 15 video selectable
                photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.VIDEO_ONLY, 15))
            }) {
                Text("Select 15 video max")
            }

            Button(onClick = {
                // Launch the picker with 5 max images & video selectable
                photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_AND_VIDEO, 5))
            }) {
                Text("Select 5 images & video max")
            }
        }
    }
    ```

=== "Views"

    ```kotlin
    class
    // Register a callback for the Activity Result
    val photoPicker = registerForActivityResult(PhotoPicker()) { uris ->
        // uris contain the list of selected images & video
        println(uris)
    }


    // Launch the picker with only one image selectable
    photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_ONLY, 1))

    // Launch the picker with 5 video selectable
    photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.VIDEO_ONLY, 5))

    // Launch the picker with 15 max images & video selectable
    photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_AND_VIDEO, 15))
    ```

[api_reference]: /modernstorage/api/photopicker/
[activity_result_api]: https://developer.android.com/training/basics/intents/result#register
[ads_photo_picker]: https://youtu.be/hBVwr2ErQCw?t=907
