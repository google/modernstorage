![ModernStorage](docs/images/favicon.png)
# ModernStorage

ModernStorage is a group of libraries that provide an abstraction layer over storage on Android to
simplify its interactions by apps developers. Coil is:

- **Fast**: Coil performs a number of optimizations including memory and disk caching, downsampling the image in memory, re-using bitmaps, automatically pausing/cancelling requests, and more.
- **Lightweight**: Coil adds ~2000 methods to your APK (for apps that already use OkHttp and Coroutines), which is comparable to Picasso and significantly less than Glide and Fresco.
- **Easy to use**: Coil's API leverages Kotlin's language features for simplicity and minimal boilerplate.
- **Modern**: Coil is Kotlin-first and uses modern libraries including Coroutines, OkHttp, Okio, and AndroidX Lifecycles.

## Download

Coil is available on `mavenCentral()`.

```kotlin
implementation("com.google.modernstorage:mediastore:1.0.0-alpha01")
implementation("com.google.modernstorage:saf:1.0.0-alpha01")
```

## Quick Start

To load an image into an `ImageView`, use the `load` extension function:

```kotlin
// URL
imageView.load("https://www.example.com/image.jpg")

// Resource
imageView.load(R.drawable.image)

// File
imageView.load(File("/path/to/image.jpg"))

// And more...
```

Requests can be configured with an optional trailing lambda:

```kotlin
imageView.load("https://www.example.com/image.jpg") {
    crossfade(true)
    placeholder(R.drawable.image)
    transformations(CircleCropTransformation())
}
```

## Is it ready?
Not yet! We've just started uploading our work but we will have proper documentation, tests and releases really soon!

## Contributions

Please contribute! We will gladly review any pull requests.
Make sure to read the [Contributing](CONTRIBUTING.md) page first though.

## License

```
Copyright 2021 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
