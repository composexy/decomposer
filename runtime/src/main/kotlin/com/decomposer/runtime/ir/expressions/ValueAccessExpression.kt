package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.ValueSymbol
import kotlinx.serialization.Serializable

@Serializable
abstract class ValueAccessExpression : DeclarationReference() {
    abstract override var symbol: ValueSymbol
    abstract var origin: StatementOrigin?
}
