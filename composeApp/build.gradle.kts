import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    java
    alias(libs.plugins.protobuf)
    kotlin(libs.plugins.kotlin.serialization.get().pluginId) version libs.versions.kotlin
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.2" // Protocol Buffers compiler
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                kotlin {

                }
            }
        }
    }
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        desktopMain.dependencies {
            implementation(libs.protobuf.kotlin)
            implementation(projects.runtime)
            implementation(libs.kotlin.compilerEmbeddable)
            implementation(libs.ktor.server.core)
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.websockets)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.decomposer.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.decomposer"
            packageVersion = "1.0.0"
        }
    }
}
