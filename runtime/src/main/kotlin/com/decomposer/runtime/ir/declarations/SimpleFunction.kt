package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.PropertySymbol
import com.decomposer.runtime.ir.symbols.SimpleFunctionSymbol

abstract class SimpleFunction : Function(), OverridableDeclaration<SimpleFunctionSymbol>,
    AttributeContainer {
    abstract override val symbol: SimpleFunctionSymbol
    abstract override var overriddenSymbols: List<SimpleFunctionSymbol>
    abstract var isTailrec: Boolean
    abstract var isSuspend: Boolean
    abstract var isOperator: Boolean
    abstract var isInfix: Boolean
    abstract var correspondingPropertySymbol: PropertySymbol?
}
