![ModernStorage](docs/images/favicon.png)
# ModernStorage

ModernStorage is a group of libraries that provide an abstraction layer over storage on Android to
simplify its interactions by apps developers. ModernStorage is:

- **Easy to use**: ModernStorage focuses on API simplicity. Rather than calling four separate methods with the Android Framework API, you only need to call one.
- **Opinionated**: ModernStorage is written by the Android DevRel team in collaboration with the Android Storage team, taking in account all the feedback from the developer community to address common issues when dealing with storage on Android.

## Download

ModernStorage is available on `mavenCentral()`.

```kotlin
// For MediaStore interactions
implementation("com.google.modernstorage:modernstorage-mediastore:1.0.0-alpha02")

// For Storage Access Framework interactions on API 26+
implementation("com.google.modernstorage:modernstorage-filesystem:1.0.0-alpha02")
```

## Quick Start

* For MediaStore interactions, check out the [sample app](/sample/src/main/java/com/google/modernstorage/sample/mediastore/)
* For non-media files on shared storage & SAF interactions, stay tuned for the upcoming **filesystem** package

## Contributions

We're still at an early stage sharing the vision of ModernStorage and would love to have more feature requests and ideas proposed as issues.
We would be glad to review pull requests, but keep in mind that we want to minimize expanding the API surface until we get more feedback from developers.
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
