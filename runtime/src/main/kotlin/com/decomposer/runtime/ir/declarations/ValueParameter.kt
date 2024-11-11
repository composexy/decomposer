package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.expressions.ExpressionBody
import com.decomposer.runtime.ir.symbols.ValueParameterSymbol
import kotlinx.serialization.Serializable

@Serializable
data class ValueParameter(
    val isAssignable: Boolean,
    override val symbol: ValueParameterSymbol,
    val varargElementType: Type?,
    val isCrossinline: Boolean,
    val isNoinline: Boolean,
    val isHidden: Boolean,
    val defaultValue: ExpressionBody?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: DeclarationParent,
    override val annotations: List<ConstructorCall>,
    override val type: Type,
    override val name: Name
) : DeclarationBase, ValueDeclaration
