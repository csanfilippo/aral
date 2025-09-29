import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "it.calogerosanfilippo"
version = "0.1.2"

kotlin {

    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    explicitApi()

    sourceSets {

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
        }

        iosMain.dependencies {
            api(libs.nserrorkt)
        }

        commonTest.dependencies {
            implementation(libs.turbine)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.coroutines.test)
        }

    }
}

android {
    namespace = "it.calogerosanfilippo.aral"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "aral", version.toString())

    pom {
        name = "Aral"
        description = "A Kotlin Multiplatform library to parse XML"
        inceptionYear = "2025"
        url = "https://github.com/csanfilippo/aral"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "csanfilippo"
                name = "Calogero Sanfilippo"
                url = "https://github.com/csanfilippo"
            }
        }
        scm {
            url = "https://github.com/csanfilippo/aral"
            connection = "scm:git:git://github.com/csanfilippo/aral.git"
            developerConnection = "scm:git:ssh://git@github.com/csanfilippo/aral.git"
        }
    }
}
