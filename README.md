<div align="center">
  <img src="banner.svg" alt="Cosmix Gradle Plugin Banner" />

  <br/><br/>

  [![License: GPL 3.0](https://img.shields.io/badge/License-GPL%203.0-blue.svg?style=for-the-badge)](https://opensource.org/licenses/GPL-3.0)
  [![Java 17](https://img.shields.io/badge/Java-17-orange.svg?style=for-the-badge&logo=java)](https://adoptium.net/)
  [![Gradle](https://img.shields.io/badge/Gradle-8+-02303A.svg?style=for-the-badge&logo=gradle)](https://gradle.org)

  <br/>
  <b>Official Gradle plugin for building Cosmix extensions (<code>.csx</code> format).</b>
</div>

## What is this?

This plugin compiles your Kotlin extension code into the `.csx` format required by the Cosmix streaming app.
A `.csx` file contains:
- `android.dex` — for Android & Android TV
- `desktop.jar` — for Windows
- `manifest.json` — extension metadata

## Requirements

- Java 17+
- Gradle 8+
- Android SDK

## Setup

Add JitPack to your `settings.gradle.kts`:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("app.cosmix.gradle") version "1.0.0"
}
```

## Usage

Build your extension:
```bash
./gradlew YourExtension:makeCsx
```

Generate plugins.json:
```bash
./gradlew makePluginsJson
```

## Building an Extension

Extend `CsxApi()` in your Kotlin file:

```kotlin
class MyProvider : CsxApi() {
    override val name = "My Extension"
    override val mainUrl = "https://example.com"
    override val lang = "en"
}
```

Use the official template:
[github.com/cosmix-app/cosmix-extension-template](https://github.com/cosmix-app/cosmix-extension-template)

## License

GPL-3.0 © 2026 Cosmix
