package com.decomposer.compiler

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmSerializeIrMode
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.name.ClassId

abstract class BaseDecomposerLowering(
    private val messageCollector: MessageCollector,
    private val context: IrPluginContext
) : IrElementTransformerVoidWithContext() {
    protected fun log(message: String) {
        messageCollector.report(CompilerMessageSeverity.LOGGING, message)
    }

    protected fun withSerializeIrOption(
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

    protected fun getTopLevelClass(classId: ClassId): IrClassSymbol {
        return context.referenceClass(classId)
            ?: error("Class not find ${classId.asSingleFqName()} in classpath!")
    }

    protected fun irConst(value: String): IrConst {
        return IrConstImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            context.irBuiltIns.stringType,
            IrConstKind.String,
            value
        )
    }
}
