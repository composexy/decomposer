package com.decomposer.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.JvmIrSerializerImpl
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors

class PostComposeIrStorageLowering(
    messageCollector: MessageCollector,
    private val configuration: CompilerConfiguration,
    context: IrPluginContext
) : BaseDecomposerLowering(messageCollector, context) {
    private val irSerializer = JvmIrSerializerImpl(configuration)
    private val postComposeIrClass = getTopLevelClass(CLASS_ID_POST_COMPOSE_IR)

    override fun visitModuleFragment(declaration: IrModuleFragment): IrModuleFragment {
        return super.visitModuleFragment(declaration)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitFileNew(declaration: IrFile): IrFile {
        val serializedIr = withSerializeIrOption(configuration) {
            irSerializer.serializeIrFile(declaration)
        }

        val annotation = IrConstructorCallImpl(
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            type = postComposeIrClass.defaultType,
            symbol = postComposeIrClass.constructors.first(),
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0
        )

        annotation.putValueArgument(0, irConst(serializedIr.contentToString()))
        declaration.annotations += annotation

        return declaration
    }
}
