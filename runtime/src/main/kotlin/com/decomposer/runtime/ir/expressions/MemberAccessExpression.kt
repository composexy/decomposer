package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.symbols.Symbol
import kotlinx.serialization.Serializable

@Serializable
abstract class MemberAccessExpression<S : Symbol> : DeclarationReference() {
    abstract val dispatchReceiver: Expression?
    abstract val extensionReceiver: Expression?
    abstract override val symbol: S
    abstract val origin: StatementOrigin?
    protected abstract val valueArguments: List<Expression?>
    protected abstract val typeArguments: List<Type?>
    internal val targetContextParameterCount: Int get() = -1
    internal val targetHasDispatchReceiver: Boolean get() = false
    internal val targetHasExtensionReceiver: Boolean get() = false
}
