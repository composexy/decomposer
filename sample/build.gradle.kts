plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.decomposer.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.decomposer.sample"
        minSdk = 24
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
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-P", "plugin:com.decomposer.compiler:enabled=true")
        freeCompilerArgs.addAll("-P", "plugin:com.decomposer.compiler:preComposeIrStorageEnabled=true")
        freeCompilerArgs.addAll("-P", "plugin:com.decomposer.compiler:postComposeIrStorageEnabled=true")
    }
}

dependencies {
    platform(libs.compose.bom)
    implementation(libs.androidx.activity.compose)
    implementation(projects.runtime)
    kotlinCompilerPluginClasspath(projects.compilerPlugin)
    testImplementation(libs.junit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.espresso.core)
}
