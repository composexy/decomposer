package com.decomposer.compiler

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class PostComposeIrStorageLowering(
    messageCollector: MessageCollector
) : BaseDecomposerLowering(messageCollector) {
    override fun visitModuleFragment(declaration: IrModuleFragment): IrModuleFragment {
        return super.visitModuleFragment(declaration)
    }
}
