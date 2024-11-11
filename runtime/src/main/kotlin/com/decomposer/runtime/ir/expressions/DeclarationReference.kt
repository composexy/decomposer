package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.Symbol
import kotlinx.serialization.Serializable

@Serializable
abstract class DeclarationReference : Expression() {
    abstract val symbol: Symbol
}
