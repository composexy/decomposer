package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import com.decomposer.runtime.ir.symbols.FunctionSymbol

data class FunctionReference(
    val reflectionTarget: FunctionSymbol?,
    override val startOffset: Int,
    override var symbol: FunctionSymbol,
    override val dispatchReceiver: Expression?,
    override val extensionReceiver: Expression?,
    override val origin: StatementOrigin?,
    override val valueArguments: List<Expression?>,
    override val typeArguments: List<Type?>,
    override val type: Type,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : CallableReference<FunctionSymbol>()
