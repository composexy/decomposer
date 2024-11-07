package com.decomposer.compiler

import org.jetbrains.kotlin.backend.jvm.JvmIrSerializer
import org.jetbrains.kotlin.backend.jvm.JvmIrSerializerImpl
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class PreComposeIrStorageLowering(
    messageCollector: MessageCollector,
    private val configuration: CompilerConfiguration
) : BaseDecomposerLowering(messageCollector) {

    private lateinit var irSerializer: JvmIrSerializer

    override fun visitModuleFragment(declaration: IrModuleFragment): IrModuleFragment {
        val irSerializer = JvmIrSerializerImpl(configuration)
        return super.visitModuleFragment(declaration)
    }
}
