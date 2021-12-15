![ModernStorage](images/favicon.png)
# ModernStorage

ModernStorage is a group of libraries that provide an abstraction layer over storage on Android to
simplify its interactions by apps developers. ModernStorage is:

- **Easy to use**: ModernStorage focuses on API simplicity. Rather than calling four separate methods with the Android Framework API, you only need to call one.
- **Opinionated**: ModernStorage is written by the Android DevRel team in collaboration with the Android Storage team, taking in account all the feedback from the developer community to address common issues when dealing with storage on Android.

## Download

ModernStorage is available on `mavenCentral()`.

```kotlin
// For storage permissions checking
implementation("com.google.modernstorage:modernstorage-permissions:{{ lib_version }}")

// For storage interactions using Okio FileSystem API
implementation("com.google.modernstorage:modernstorage-storage:{{ lib_version }}")
```

## Quick start

* Have a look at the [permissions][permissions_guide] and [storage interactions][storage_interactions_guide] guides
* Check out the [sample app][sample_app]

## Is it ready?
It's in progress! Our current version is **{{ lib_version }}**. As it's an alpha release, we're
expecting API breaking changes between releases. While it always seems risky to rely on an
experimental library, we're enforcing storage best practises (including Scoped Storage), which
developers aren't always aware of.

We're looking for a stable release in 2022 (we don't have yet a precise date). We actively
listen to your feedback to make ModernStorage the default library for storage interactions on
Android.

## Contributions

We're still at an early stage sharing the vision of ModernStorage and would love to have more
feature requests and ideas proposed as issues. We would be glad to review pull requests, but keep in
mind that we want to minimize expanding the API surface until we get more feedback from developers.
Make sure to read the [Contributing][contributing] page first though.

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

[sample_app]: https://github.com/google/modernstorage/tree/main/sample/src/main/java/com/google/modernstorage/sample/mediastore/
[contributing]: https://github.com/google/modernstorage/blob/main/CONTRIBUTING.md
