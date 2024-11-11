package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.IrSymbol

sealed interface IrOverridableDeclaration<S : IrSymbol> : IrOverridableMember {
    override var startOffset: Int
    override var endOffset: Int
    override val symbol: S
    var isFakeOverride: Boolean
    var overriddenSymbols: List<S>
}
