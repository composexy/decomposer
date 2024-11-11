package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.IrBody
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.symbols.IrConstructorSymbol

data class IrConstructor(
    override val symbol: IrConstructorSymbol,
    val isPrimary: Boolean,
    override val annotations: List<IrConstructorCall>,
    override val isInline: Boolean,
    override val isExpect: Boolean,
    override val returnType: IrType,
    override val dispatchReceiverParameter: IrValueParameter?,
    override val extensionReceiverParameter: IrValueParameter?,
    override val contextReceiverParametersCount: Int,
    override val body: IrBody?,
    override val attributeMap: List<Any?>?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: IrDeclarationParent,
    override val isExternal: Boolean,
    override val name: Name,
    override val visibility: Visibility,
    override val typeParameters: List<IrTypeParameter>
) : IrFunction()
