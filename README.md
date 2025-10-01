# aral

**aral** is a Kotlin Multiplatform library for parsing XML. It supports Android, iOS, macOS, watchOS and tvOS. More platform will come.

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [License](#license)
- [Apps using aral](#apps-using-aral)

## Installation

**aral** is currently published to Maven Central, so add it to your project's repositories.

```kotlin
repositories {
    mavenCentral()
    // ...
}
```

Then, add the dependency to your common source set's dependencies.

```kotlin
commonMain {
    dependencies {
        // ...
        implementation("it.calogerosanfilippos:aral:0.2.0")
    }
}
```

## Usage

### Creating an XMLParser instance

An `XMLParser` instance is the entry point of the library. It implements a push parser, emitting a flow of parsing events.

It's possible to create an instance of `XMLParser` directly in the common code, without having to pass any platform-specific dependencies.

```kotlin
val xmlParser: XMLParser = XMLParserFactory.getParser()
```

### XML Parsing from string

To parse an XML string, the `parse` function can be used.

```kotlin
val xmlString: String = "xml-string"
val eventFlow: Flow<XMLParserEvent> = xmlParser.parse(xmlString)
```
The function returns a `Flow<XMLParserEvent>`.

```kotlin
sealed class XMLParserEvent {
    data object DocumentStart : XMLParserEvent()
    data object DocumentEnd : XMLParserEvent()
    data class ElementStartFound(val name: String, val attributes: Map<String, String>): XMLParserEvent()

    data class ElementEndFound(val name: String) : XMLParserEvent()
    data class CharactersFound(val characters: String) : XMLParserEvent()
    data class Error(val exception: Exception) : XMLParserEvent()
}
```

## License

```
Copyright 2024-2025 Calogero Sanfilippo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Apps using aral

<details>
  <summary>List of Apps using aral</summary>

* [TurinPark](https://calogerosanfilippo.it/apps/turinpark/)

</details>
