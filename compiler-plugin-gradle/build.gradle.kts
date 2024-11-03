import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(libs.plugins.mavenPublish)
}

sourceSets {
    main { java.srcDir(layout.buildDirectory.dir("generated/sources/version-templates/kotlin/main")) }
}

val copyVersionTemplatesProvider =
    tasks.register<Copy>("copyVersionTemplates") {
        inputs.property("version", project.property("VERSION_NAME"))
        from(project.layout.projectDirectory.dir("version-templates"))
        into(project.layout.buildDirectory.dir("generated/sources/version-templates/kotlin/main"))
        expand(mapOf("projectVersion" to "${project.property("VERSION_NAME")}"))
        filteringCharset = "UTF-8"
    }

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(copyVersionTemplatesProvider)
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_1_8)
        apiVersion.set(KotlinVersion.KOTLIN_1_8)
    }
}

gradlePlugin {
    plugins {
        create("decomposer") {
            id = "com.decomposer.compiler"
            implementationClass = "com.decomposer.gradle.DecomposerGradlePlugin"
        }
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin.api)
    compileOnly(libs.kotlin.stdlib)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions { moduleName.set(project.property("POM_ARTIFACT_ID") as String) }
}
