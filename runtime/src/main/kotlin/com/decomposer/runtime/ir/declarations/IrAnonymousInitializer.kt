package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.IrBlockBody
import com.decomposer.runtime.ir.symbols.IrAnonymousInitializerSymbol

abstract class IrAnonymousInitializer : IrDeclarationBase() {
    abstract override val symbol: IrAnonymousInitializerSymbol
    abstract var isStatic: Boolean
    abstract var body: IrBlockBody
}
