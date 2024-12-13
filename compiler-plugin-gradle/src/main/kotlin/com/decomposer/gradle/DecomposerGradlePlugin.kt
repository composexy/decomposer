package com.decomposer.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider

import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class DecomposerGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create("decomposer", DecomposerPluginExtension::class.java)
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(DecomposerPluginExtension::class.java)
        val enabled = extension.enabled.get()
        return project.provider {
            listOf(SubpluginOption(key = "enabled", value = enabled.toString()))
        }
    }

    override fun getCompilerPluginId() = "com.decomposer.compiler"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "io.github.composexy-decomposer",
            artifactId = "decomposer-compiler",
            version = VERSION
        )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

    companion object {
        const val VERSION = "0.1.0-alpha1"
    }
}
