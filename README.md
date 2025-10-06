# aral
![Maven Central](https://img.shields.io/maven-central/v/it.calogerosanfilippo/aral)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

**aral** is a lightweight, efficient Kotlin Multiplatform library for XML parsing. Built for the modern, multi-platform world, it offers a simple, flow-based API for parsing XML documents across Android, JVM, iOS, macOS, watchOS, and tvOS.

## Features

*   **Kotlin Multiplatform:** Write your parsing logic once and run it everywhere.
*   **Flow-Based API:** A modern, asynchronous way to handle XML parsing events.
*   **Lightweight:** No heavy dependencies, keeping your app size down.
*   **Easy to Use:** A simple and intuitive API that's easy to get started with.

## Installation

**aral** is available on Maven Central.

1.  Add Maven Central to your `settings.gradle.kts` or `build.gradle.kts` file if it's not already there:

    ```kotlin
    repositories {
        mavenCentral()
    }
    ```

2.  Add the dependency to your `commonMain` source set:

    ```kotlin
    commonMain {
        dependencies {
            implementation("it.calogerosanfilippo:aral:0.2.0")
        }
    }
    ```

## Usage

The entry point to the library is the `XMLParser`. You can easily get an instance of it in your common code.

### Creating an `XMLParser`

```kotlin
import it.calogerosanfilippo.aral.xml.XMLParserFactory

val xmlParser = XMLParserFactory.getParser()
```

### Parsing an XML String

The `parse` method takes an XML string and returns a `Flow` of `XMLParserEvent`s. You can then collect the flow to handle each event.

```kotlin
import it.calogerosanfilippo.aral.xml.XMLParserEvent
import kotlinx.coroutines.flow.collect

val xmlString = """
<root>
    <item id="1">Hello</item>
    <item id="2">World</item>
</root>
"""

val eventFlow = xmlParser.parse(xmlString)

eventFlow.collect { event ->
    when (event) {
        is XMLParserEvent.DocumentStart -> println("Document parsing started")
        is XMLParserEvent.DocumentEnd -> println("Document parsing finished")
        is XMLParserEvent.ElementStartFound -> println("Element started: ${event.name}, attributes: ${event.attributes}")
        is XMLParserEvent.ElementEndFound -> println("Element ended: ${event.name}")
        is XMLParserEvent.CharactersFound -> println("Characters found: ${event.characters}")
        is XMLParserEvent.Error -> println("An error occurred: ${event.exception}")
    }
}
```

The `XMLParserEvent` is a sealed class that represents all possible events during parsing:

```kotlin
sealed class XMLParserEvent {
    data object DocumentStart : XMLParserEvent()
    data object DocumentEnd : XMLParserEvent()
    data class ElementStartFound(val name: String, val attributes: Map<String, String>) : XMLParserEvent()
    data class ElementEndFound(val name: String) : XMLParserEvent()
    data class CharactersFound(val characters: String) : XMLParserEvent()
    data class Error(val exception: Exception) : XMLParserEvent()
}
```

## Apps Using Aral

<details>
  <summary>List of Apps</summary>

*   [TurinPark](https://calogerosanfilippo.it/apps/turinpark/)

</details>

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