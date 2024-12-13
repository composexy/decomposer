import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.wire)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin(libs.plugins.kotlinx.serialization.get().pluginId) version libs.versions.kotlin
}

wire {
    kotlin {}
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

group = "com.decomposer.runtime"

kotlin {
    jvm("desktop")

    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serializationJson)
                implementation(libs.squareup.wire.runtime)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.websocket)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.androidx.collection.jvm)
                implementation(libs.kotlin.reflect)
                implementation(compose.runtime)
                implementation(compose.ui)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.dexlib2)
            }
        }
        val androidUnitTest by getting {
            dependencies {}
        }
        val desktopMain by getting {
            dependencies {}
        }
        val desktopTest by getting {
            dependencies {}
        }
    }
}

android {
    namespace = "com.decomposer.runtime"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
