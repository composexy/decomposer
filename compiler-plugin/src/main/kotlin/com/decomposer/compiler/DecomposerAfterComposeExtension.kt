package com.decomposer.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class DecomposerAfterComposeExtension(
    private val messageCollector: MessageCollector,
    private val configuration: CompilerConfiguration
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val irStorageEnabled = configuration[KEY_IR_STORAGE_ENABLED] == true
        if (irStorageEnabled) {
            moduleFragment.transform(IrStorageLowering(messageCollector), null)
        }
    }
}
