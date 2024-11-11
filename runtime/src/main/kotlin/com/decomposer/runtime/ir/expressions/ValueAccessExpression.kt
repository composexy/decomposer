package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.ValueSymbol

abstract class ValueAccessExpression : DeclarationReference() {
    abstract override var symbol: ValueSymbol
    abstract var origin: StatementOrigin?
}
