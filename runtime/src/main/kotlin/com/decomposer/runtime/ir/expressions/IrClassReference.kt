package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.symbols.IrClassifierSymbol

abstract class IrClassReference : IrDeclarationReference() {
    abstract override var symbol: IrClassifierSymbol
    abstract var classType: IrType
}
