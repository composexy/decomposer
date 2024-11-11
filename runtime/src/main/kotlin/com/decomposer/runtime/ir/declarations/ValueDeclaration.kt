package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.symbols.ValueSymbol
import kotlinx.serialization.Serializable

@Serializable
sealed interface ValueDeclaration : DeclarationWithName, SymbolOwner {
    override val symbol: ValueSymbol
    val type: Type
}
