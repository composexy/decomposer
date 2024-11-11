package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.IrSymbol

abstract class IrCallableReference<S : IrSymbol> : IrMemberAccessExpression<S>() {
    abstract override val symbol: S
}
