package com.decomposer.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.serialization.CompatibilityMode
import org.jetbrains.kotlin.backend.common.serialization.DeclarationTable
import org.jetbrains.kotlin.backend.common.serialization.IrFileSerializer
import org.jetbrains.kotlin.backend.jvm.JvmIrSerializerImpl
import org.jetbrains.kotlin.backend.jvm.serialization.JvmGlobalDeclarationTable
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.metadata.jvm.deserialization.BitEncoding
import org.jetbrains.kotlin.metadata.jvm.deserialization.bytesToStrings

class PreComposeIrStorageLowering(
    messageCollector: MessageCollector,
    private val configuration: CompilerConfiguration,
    pluginContext: IrPluginContext
) : BaseDecomposerLowering(messageCollector, pluginContext) {

    private val irFileSerializer = IrFileSerializer(
        declarationTable = DeclarationTable(JvmGlobalDeclarationTable()),
        compatibilityMode = CompatibilityMode.CURRENT,
        languageVersionSettings = configuration.languageVersionSettings,
        sourceBaseDirs = emptyList()
    )

    private val preComposeIrClass = getTopLevelClass(CLASS_ID_PRE_COMPOSE_IR)

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitFileNew(declaration: IrFile): IrFile {
        val serializedIr = irFileSerializer.serializeIrFile(declaration)

        val annotation = IrConstructorCallImpl(
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            type = preComposeIrClass.defaultType,
            symbol = preComposeIrClass.constructors.first(),
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0
        )

        annotation.putValueArgument(0, irStringArray(BitEncoding.encodeBytes(serializedIr.fileData)))
        annotation.putValueArgument(1, irConst(serializedIr.fqName))
        annotation.putValueArgument(2, irConst(serializedIr.path))
        annotation.putValueArgument(3, irStringArray(BitEncoding.encodeBytes(serializedIr.types)))
        annotation.putValueArgument(4, irStringArray(BitEncoding.encodeBytes(serializedIr.signatures)))
        annotation.putValueArgument(5, irStringArray(BitEncoding.encodeBytes(serializedIr.strings)))
        annotation.putValueArgument(6, irStringArray(BitEncoding.encodeBytes(serializedIr.bodies)))
        annotation.putValueArgument(7, irStringArray(BitEncoding.encodeBytes(serializedIr.declarations)))
        annotation.putValueArgument(
            8,
            serializedIr.debugInfo?.let {
                irStringArray(BitEncoding.encodeBytes(it))
            } ?: irStringArray(emptyArray())
        )
        annotation.putValueArgument(
            9,
            serializedIr.backendSpecificMetadata?.let {
                irStringArray(BitEncoding.encodeBytes(it))
            } ?: irStringArray(emptyArray())
        )

        declaration.annotations += annotation

        return declaration
    }
}
