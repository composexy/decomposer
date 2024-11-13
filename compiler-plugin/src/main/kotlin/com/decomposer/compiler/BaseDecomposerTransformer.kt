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
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.name.ClassId

abstract class BaseDecomposerTransformer(
    private val messageCollector: MessageCollector,
    private val context: IrPluginContext
) : IrElementTransformerVoidWithContext() {
    protected fun log(message: String) {
        messageCollector.report(CompilerMessageSeverity.LOGGING, message)
    }

    protected fun <R> withSerializeIrOption(
        compilerConfiguration: CompilerConfiguration,
        block: () -> R
    ): R {
        val previous = compilerConfiguration[JVMConfigurationKeys.SERIALIZE_IR]
            ?: JvmSerializeIrMode.NONE
        val result = try {
            compilerConfiguration.put(JVMConfigurationKeys.SERIALIZE_IR, JvmSerializeIrMode.ALL)
            block()
        } finally {
            compilerConfiguration.put(JVMConfigurationKeys.SERIALIZE_IR, previous)
        }
        return result
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

    protected fun irConst(value: Boolean): IrConst {
        return IrConstImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            context.irBuiltIns.booleanType,
            IrConstKind.Boolean,
            value
        )
    }

    protected fun irStringArray(value: Array<String>): IrExpression {
        val builtIns = context.irBuiltIns
        val arrayType = builtIns.arrayClass.typeWith(builtIns.stringType)
        return IrVarargImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            type = arrayType,
            varargElementType = builtIns.stringType,
            elements = value.map { irConst(it) }
        )
    }
}
