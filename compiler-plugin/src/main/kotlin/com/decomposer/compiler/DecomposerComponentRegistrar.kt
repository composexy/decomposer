@file:Suppress("DEPRECATION", "UnstableApiUsage")

package com.decomposer.compiler

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.LoadingOrder
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
@AutoService(ComponentRegistrar::class)
class DecomposerComponentRegistrar : ComponentRegistrar {
    override val supportsK2: Boolean
        get() = true

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        if (configuration[KEY_ENABLED] == false) return
        val messageCollector = configuration.get(
            CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        project.extensionArea.getExtensionPoint(IrGenerationExtension.extensionPointName).run {
            registerExtension(
                DecomposerBeforeComposeExtension(messageCollector, configuration),
                LoadingOrder.FIRST,
                project
            )
            registerExtension(
                DecomposerAfterComposeExtension(messageCollector, configuration),
                LoadingOrder.LAST,
                project
            )
        }
    }
}
