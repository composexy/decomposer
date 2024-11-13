import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.protobuf)
    kotlin(libs.plugins.kotlinx.serialization.get().pluginId) version libs.versions.kotlin
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

group = "com.decomposer.runtime"
version = "0.0.1"

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java")
            }
            task.inputs.files(file("src/commonMain"))
        }
    }
}

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
                api(libs.protobuf.java)
                implementation(libs.protobuf.java.util)
                implementation(libs.kotlinx.serializationJson)
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
            dependencies {
            }
        }
        val desktopMain by getting {
            dependencies {
            }
        }
        val desktopTest by getting {
            dependencies {
            }
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
