package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.Symbol

abstract class DeclarationReference : Expression() {
    abstract val symbol: Symbol
}
