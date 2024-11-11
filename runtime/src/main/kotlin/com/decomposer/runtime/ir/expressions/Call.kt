package com.decomposer.runtime.ir.expressions

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.declarations.AttributeContainer
import com.decomposer.runtime.ir.symbols.ClassSymbol
import com.decomposer.runtime.ir.symbols.SimpleFunctionSymbol
import kotlinx.serialization.Serializable

@Serializable
data class Call(
    override val symbol: SimpleFunctionSymbol,
    val superQualifierSymbol: ClassSymbol?,
    override val startOffset: Int,
    override val dispatchReceiver: Expression?,
    override val extensionReceiver: Expression?,
    override val origin: StatementOrigin?,
    override val valueArguments: List<Expression?>,
    override val typeArguments: List<Type?>,
    override val type: Type,
    override val endOffset: Int,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : FunctionAccessExpression()
