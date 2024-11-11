package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.ClassSymbol
import com.decomposer.runtime.ir.symbols.FieldSymbol
import kotlinx.serialization.Serializable

@Serializable
abstract class FieldAccessExpression : DeclarationReference() {
    abstract override val symbol: FieldSymbol
    abstract val superQualifierSymbol: ClassSymbol?
    abstract val receiver: Expression?
    abstract val origin: StatementOrigin?
}
