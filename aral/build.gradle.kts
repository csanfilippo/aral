import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.dokka)
}

dependencies {
    dokkaHtmlPlugin(libs.dokka.versioning)
}

group = "it.calogerosanfilippo"
version = "1.0.1"

kotlin {

    jvm()

    jvmToolchain(21)

    android {
        namespace = "it.calogerosanfilippo.aral"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {}.configure {}

        compilations.configureEach {

            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(
                        JvmTarget.JVM_21
                    )
                }
            }
        }
    }

    iosArm64()

    iosSimulatorArm64()
    macosArm64()

    tvosArm64()
    tvosSimulatorArm64()

    watchosDeviceArm64()
    watchosSimulatorArm64()

    explicitApi()

    sourceSets {

        val javaMain by creating {
            dependsOn(commonMain.get())
        }
        jvmMain.get().dependsOn(javaMain)
        androidMain.get().dependsOn(javaMain)

        val appleMain by creating {
            dependsOn(commonMain.get())
        }
        iosArm64Main.get().dependsOn(appleMain)
        iosSimulatorArm64Main.get().dependsOn(appleMain)
        macosArm64Main.get().dependsOn(appleMain)
        tvosArm64Main.get().dependsOn(appleMain)
        tvosSimulatorArm64Main.get().dependsOn(appleMain)
        watchosDeviceArm64Main.get().dependsOn(appleMain)
        watchosSimulatorArm64Main.get().dependsOn(appleMain)

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
        }

        commonTest.dependencies {
            implementation(libs.turbine)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.coroutines.test)
        }

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

dokka {
    pluginsConfiguration {
        versioning {
            version = (project.findProperty("dokkaVersion") as? String)
                ?: project.version.toString().split(".").take(2).joinToString(".")
            (project.findProperty("dokkaOlderVersionsDir") as? String)?.let {
                olderVersionsDir = file(it)
            }
        }
    }
}
