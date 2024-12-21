plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.mavenPublish) apply false
}

subprojects {
    if (this.name != "composeApp" && this.name != "sample") {
        group = project.property("GROUP") as String
        version = project.property("VERSION_NAME") as String
    }
}
