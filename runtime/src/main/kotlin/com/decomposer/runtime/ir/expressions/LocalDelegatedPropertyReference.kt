package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import com.decomposer.runtime.ir.symbols.LocalDelegatedPropertySymbol
import com.decomposer.runtime.ir.symbols.SimpleFunctionSymbol
import com.decomposer.runtime.ir.symbols.VariableSymbol
import kotlinx.serialization.Serializable

@Serializable
data class LocalDelegatedPropertyReference(
    val delegate: VariableSymbol,
    val getter: SimpleFunctionSymbol,
    val setter: SimpleFunctionSymbol?,
    override val startOffset: Int,
    override var symbol: LocalDelegatedPropertySymbol,
    override val dispatchReceiver: Expression?,
    override val extensionReceiver: Expression?,
    override val origin: StatementOrigin?,
    override val valueArguments: List<Expression?>,
    override val typeArguments: List<Type?>,
    override val type: Type,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : CallableReference<LocalDelegatedPropertySymbol>()
