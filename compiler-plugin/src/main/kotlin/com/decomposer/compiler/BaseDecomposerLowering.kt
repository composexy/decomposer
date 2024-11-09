package com.decomposer.compiler

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.serialization.proto.FileEntry
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmSerializeIrMode
import org.jetbrains.kotlin.descriptors.containingPackage
import org.jetbrains.kotlin.descriptors.findPackage
import org.jetbrains.kotlin.ir.AbstractIrFileEntry
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.symbols.impl.IrFileSymbolImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.metadata.jvm.deserialization.bytesToStrings
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

abstract class BaseDecomposerLowering(
    private val messageCollector: MessageCollector,
    private val context: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    private val irManifestClass = getTopLevelClass(CLASS_ID_IR_MANIFEST)

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

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    protected fun irManifestFile(
        irModuleFragment: IrModuleFragment,
        fileList: List<String>
    ): IrFile {
        val irFileArray = irStringArray(fileList.toTypedArray())
        val annotation = IrConstructorCallImpl(
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            type = irManifestClass.defaultType,
            symbol = irManifestClass.constructors.first(),
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0
        )

        annotation.putValueArgument(0, irFileArray)

        val fileEntry = irEmptyFileEntry()
        return IrFileImpl(
            fileEntry,
            IrFileSymbolImpl(),
            FqName(irManifestPackage(irModuleFragment.name.asString())),
            irModuleFragment
        ).apply {
            this.annotations += annotation
        }
    }

    protected fun irEmptyFileEntry(): AbstractIrFileEntry {
        return object : AbstractIrFileEntry() {
            override val lineStartOffsets = IntArray(0)
            override val maxOffset = 0
            override val name = IR_MANIFEST_NAME
        }
    }
}
