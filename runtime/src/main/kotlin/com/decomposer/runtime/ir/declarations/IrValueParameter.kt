package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.expressions.IrExpressionBody
import com.decomposer.runtime.ir.symbols.IrValueParameterSymbol

data class IrValueParameter(
    val isAssignable: Boolean,
    override val symbol: IrValueParameterSymbol,
    val varargElementType: IrType?,
    val isCrossinline: Boolean,
    val isNoinline: Boolean,
    val isHidden: Boolean,
    val defaultValue: IrExpressionBody?,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: IrDeclarationParent,
    override val annotations: List<IrConstructorCall>,
    override val type: IrType,
    override val name: Name
) : IrDeclarationBase, IrValueDeclaration
