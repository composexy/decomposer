package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.declarations.IrFileEntry
import com.decomposer.runtime.ir.symbols.IrFunctionSymbol

abstract class IrInlinedFunctionBlock : IrBlock() {
    abstract var inlineFunctionSymbol: IrFunctionSymbol?
    abstract var fileEntry: IrFileEntry
}
