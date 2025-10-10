pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "aral"
include(":aral")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_21)) {
    "This project needs to be run with Java 21 or higher (found: ${JavaVersion.current()})."
}
