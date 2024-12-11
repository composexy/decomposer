package com.decomposer.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.JvmIrSerializerImpl
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.metadata.jvm.deserialization.BitEncoding

@OptIn(UnsafeDuringIrConstructionAPI::class)
class IrSerializeTransformer(
    composed: Boolean,
    messageCollector: MessageCollector,
    private val configuration: CompilerConfiguration,
    context: IrPluginContext
) : BaseDecomposerTransformer(messageCollector, context) {

    private val irSerializer = JvmIrSerializerImpl(configuration)
    private val postComposeIrClass = getTopLevelClass(CLASS_ID_POST_COMPOSE_IR)
    private val preComposeIrClass = getTopLevelClass(CLASS_ID_PRE_COMPOSE_IR)
    private val composeIrClass = if (composed) {
        postComposeIrClass
    } else {
        preComposeIrClass
    }

    override fun visitFileNew(declaration: IrFile): IrFile {
        return withSerializeIrOption(configuration) {
            val fileIr = irSerializer.serializeIrFile(declaration)
            val fileIrDump = withoutDecomposerAnnotations(declaration) {
                irStringArray(BitEncoding.encodeBytes(dump().encodeToByteArray()))
            }
            var dumpAnnotated = false

            if (fileIr != null) {
                declaration.annotations += irComposeIrCall().apply {
                    putValueArgument(0, irConst(declaration.fileEntry.name))
                    putValueArgument(1, irConst(declaration.packageFqName.asString()))
                    putValueArgument(2, irConst(true))
                    putValueArgument(3, fileIrDump)
                    putValueArgument(4, irStringArray(BitEncoding.encodeBytes(fileIr)))
                }
                dumpAnnotated = true
            }

            for (irClass in declaration.declarations.filterIsInstance<IrClass>()) {
                val topLevelClassIr = irSerializer.serializeTopLevelIrClass(irClass) ?: continue
                val irDump = if (dumpAnnotated) irStringArray(emptyArray()) else fileIrDump
                irClass.annotations += irComposeIrCall().apply {
                    putValueArgument(0, irConst(declaration.fileEntry.name))
                    putValueArgument(1, irConst(declaration.packageFqName.asString()))
                    putValueArgument(2, irConst(false))
                    putValueArgument(3, irDump)
                    putValueArgument(4, irStringArray(BitEncoding.encodeBytes(topLevelClassIr)))
                }
                dumpAnnotated = true
            }

            declaration
        }
    }

    private fun irComposeIrCall(): IrConstructorCall =
        IrConstructorCallImpl(
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            type = composeIrClass.defaultType,
            symbol = composeIrClass.constructors.first(),
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0
        )

    private fun <R> withoutDecomposerAnnotations(
        declaration: IrFile,
        block: IrFile.() -> R
    ): R {
        val removeOnFile = declaration.annotations.filter {
            it.type == postComposeIrClass.defaultType || it.type == preComposeIrClass.defaultType
        }
        val removeOnClasses = mutableMapOf<IrClass, List<IrConstructorCall>>()
        return try {
            declaration.annotations -= removeOnFile
            declaration.declarations.filterIsInstance<IrClass>().forEach { irClass ->
                val removeOnClass = irClass.annotations.filter {
                    it.type == postComposeIrClass.defaultType ||
                            it.type == preComposeIrClass.defaultType
                }
                removeOnClasses[irClass] = removeOnClass
                irClass.annotations -= removeOnClass
            }
            block(declaration)
        } finally {
            declaration.annotations += removeOnFile
            declaration.declarations.filterIsInstance<IrClass>().forEach { irClass ->
                removeOnClasses[irClass]?.let {
                    irClass.annotations += it
                }
            }
        }
    }
}
