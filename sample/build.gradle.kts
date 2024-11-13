import com.android.build.api.dsl.Packaging
import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.protobuf)
    kotlin(libs.plugins.kotlin.serialization.get().pluginId) version libs.versions.kotlin
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3" // Protocol Buffers compiler
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java")
            }
        }
    }
}

android {
    namespace = "com.decomposer.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.decomposer.sample"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packaging {
        exclude("kotlin/*/*")
        exclude("kotlin/*")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-P", "plugin:com.decomposer.compiler:enabled=true")
        freeCompilerArgs.addAll("-P", "plugin:com.decomposer.compiler:preComposeIrStorageEnabled=true")
        freeCompilerArgs.addAll("-P", "plugin:com.decomposer.compiler:postComposeIrStorageEnabled=true")
    }
}

dependencies {
    api(libs.protobuf.java)
    api(libs.protobuf.kotlin)
    implementation(libs.protobuf.java.util)
    implementation(libs.okhttp)
    implementation(libs.dx)
    implementation(libs.kotlin.compilerEmbeddable)
    implementation(libs.kotlinx.serializationJson)
    platform(libs.compose.bom)
    implementation(libs.dexlib2)
    implementation(libs.androidx.activity.compose)
    implementation(projects.runtime)
    kotlinCompilerPluginClasspath(projects.compilerPlugin)
    testImplementation(libs.junit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.espresso.core)
}
