package com.decomposer.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.serialization.codedInputStream
import org.jetbrains.kotlin.backend.jvm.JvmIrSerializerImpl
import org.jetbrains.kotlin.backend.jvm.serialization.proto.JvmIr
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.metadata.jvm.deserialization.bytesToStrings
import org.jetbrains.kotlin.metadata.jvm.deserialization.stringsToBytes

class PostComposeIrStorageLowering(
    messageCollector: MessageCollector,
    private val configuration: CompilerConfiguration,
    context: IrPluginContext
) : BaseDecomposerLowering(messageCollector, context) {

    private val irSerializer = JvmIrSerializerImpl(configuration)
    private val postComposeIrClass = getTopLevelClass(CLASS_ID_POST_COMPOSE_IR)


    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitFileNew(declaration: IrFile): IrFile {
        val serializedIr = withSerializeIrOption(configuration) {
            irSerializer.serializeIrFile(declaration)
        } ?: return declaration

        val annotation = IrConstructorCallImpl(
            startOffset = UNDEFINED_OFFSET,
            endOffset = UNDEFINED_OFFSET,
            type = postComposeIrClass.defaultType,
            symbol = postComposeIrClass.constructors.first(),
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0
        )

        val strings = bytesToStrings(serializedIr)
        println("Serialized string size ${strings.size}")
        val array = arrayOf(strings[0])
        val bytes = stringsToBytes(strings)
        val bytes2 = ByteArray(bytes.size - 1)
        bytes.copyInto(bytes2, 0, 1)
        //throw IllegalStateException("\n${serializedIr.contentToString()}\n${bytes2.contentToString()}")
        // Works
        // val jvmIr = JvmIr.ClassOrFile.parseFrom(serializedIr.codedInputStream)
        // Does not work
        val jvmIr = JvmIr.ClassOrFile.parseFrom(bytes2.codedInputStream)
        val stringArray = irStringArray(strings)

        annotation.putValueArgument(0, stringArray)
        declaration.annotations += annotation

        return declaration
    }
}
