package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.declarations.IrReturnTarget
import com.decomposer.runtime.ir.declarations.IrSymbolOwner
import com.decomposer.runtime.ir.symbols.IrReturnableBlockSymbol

abstract class IrReturnableBlock : IrBlock(), IrSymbolOwner, IrReturnTarget {
    abstract override val symbol: IrReturnableBlockSymbol
}
