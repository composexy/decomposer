package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.Symbol

sealed interface OverridableDeclaration<S : Symbol> : OverridableMember {
    override var startOffset: Int
    override var endOffset: Int
    override val symbol: S
    val isFakeOverride: Boolean
    val overriddenSymbols: List<S>
}
