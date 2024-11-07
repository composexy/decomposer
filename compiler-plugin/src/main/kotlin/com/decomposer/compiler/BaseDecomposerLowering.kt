package com.decomposer.compiler

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmSerializeIrMode

abstract class BaseDecomposerLowering(
    private val messageCollector: MessageCollector
) : IrElementTransformerVoidWithContext() {
    fun log(message: String) {
        messageCollector.report(CompilerMessageSeverity.LOGGING, message)
    }

    fun withSerializeIrOption(
        compilerConfiguration: CompilerConfiguration,
        block: CompilerConfiguration.() -> Unit
    ) {
        val previous = compilerConfiguration[JVMConfigurationKeys.SERIALIZE_IR]
            ?: JvmSerializeIrMode.NONE
        try {
            compilerConfiguration.put(JVMConfigurationKeys.SERIALIZE_IR, JvmSerializeIrMode.ALL)
            compilerConfiguration.apply(block)
        } finally {
            compilerConfiguration.put(JVMConfigurationKeys.SERIALIZE_IR, previous)
        }
    }
}
