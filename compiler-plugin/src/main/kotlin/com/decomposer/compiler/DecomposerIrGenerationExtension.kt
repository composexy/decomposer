package com.decomposer.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class DecomposerIrGenerationExtension(
    private val irStorageEnabled: Boolean = true,
    private val sourceStorageEnabled: Boolean = true,
    private val messageCollector: MessageCollector
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        if (irStorageEnabled) {
            moduleFragment.transform(IrStorageLowering(messageCollector), null)
        }
        if (sourceStorageEnabled) {
            moduleFragment.transform(SourceStorageLowering(messageCollector), null)
        }
    }
}