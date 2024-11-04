package com.decomposer.compiler

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

abstract class BaseDecomposerLowering(
    private val messageCollector: MessageCollector
) : IrElementTransformerVoidWithContext() {
    fun log(message: String) {
        messageCollector.report(CompilerMessageSeverity.LOGGING, message)
    }
}
