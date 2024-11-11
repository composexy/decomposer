package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.symbols.Symbol
import kotlinx.serialization.Serializable

@Serializable
abstract class CallableReference<S : Symbol> : MemberAccessExpression<S>() {
    abstract override val symbol: S
}
