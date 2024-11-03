plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
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
    compileOnly(libs.kotlin.compilerEmbeddable)
    compileOnly(libs.kotlin.stdlib)
    implementation(libs.autoService)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.compilerEmbeddable)
    testImplementation(libs.junit)
}