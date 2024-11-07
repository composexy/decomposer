package com.decomposer.compiler

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrFile

class PostComposeIrStorageLowering(
    messageCollector: MessageCollector
) : BaseDecomposerLowering(messageCollector) {
    override fun visitFileNew(declaration: IrFile): IrFile {
        println("Running IrStorageLowering")
        return declaration
    }
}
