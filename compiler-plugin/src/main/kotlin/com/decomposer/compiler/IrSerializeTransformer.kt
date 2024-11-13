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
import org.jetbrains.kotlin.metadata.jvm.deserialization.BitEncoding

@OptIn(UnsafeDuringIrConstructionAPI::class)
class IrSerializeTransformer(
    private val composed: Boolean,
    messageCollector: MessageCollector,
    private val configuration: CompilerConfiguration,
    context: IrPluginContext
) : BaseDecomposerTransformer(messageCollector, context) {

    private val irSerializer = JvmIrSerializerImpl(configuration)
    private val composeIrClass = getTopLevelClass(CLASS_ID_COMPOSE_IR)

    override fun visitFileNew(declaration: IrFile): IrFile {
        return withSerializeIrOption(configuration) {
            val fileIr = irSerializer.serializeIrFile(declaration)

            if (fileIr != null) {
                declaration.annotations += irComposeIrCall().apply {
                    putValueArgument(0, irConst(composed))
                    putValueArgument(1, irStringArray(BitEncoding.encodeBytes(fileIr)))
                }
            }

            for (irClass in declaration.declarations.filterIsInstance<IrClass>()) {
                val topLevelClassIr = irSerializer.serializeTopLevelIrClass(irClass) ?: continue
                irClass.annotations += irComposeIrCall().apply {
                    putValueArgument(0, irConst(composed))
                    putValueArgument(1, irStringArray(BitEncoding.encodeBytes(topLevelClassIr)))
                }
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
}
