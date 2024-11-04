package com.decomposer.gradle

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class DecomposerGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(DecomposerPluginExtension::class.java)

        project.dependencies.add(
            kotlinCompilation.implementationConfigurationName,
            "com.decomposer:runtime:0.1.0",
        )

        val enabled = extension.enabled.get()
        val irStorageEnabled = extension.irStorageEnabled.get()
        val sourceStorageEnabled = extension.sourceStorageEnabled.get()

        return project.provider {
            listOf(
                SubpluginOption(key = "enabled", value = enabled.toString()),
                SubpluginOption(key = "irStorageEnabled", value = irStorageEnabled.toString()),
                SubpluginOption(key = "sourceStorageEnabled", value = sourceStorageEnabled.toString())
            )
        }
    }

    override fun getCompilerPluginId() = "com.decomposer.compiler"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "com.decomposer",
            artifactId = "compiler",
            version = "0.1.0"
        )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true
}
