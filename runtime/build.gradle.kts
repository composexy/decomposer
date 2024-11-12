plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    kotlin(libs.plugins.kotlin.serialization.get().pluginId) version libs.versions.kotlin
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.dx)
    implementation(libs.kotlinx.serializationJson)
}