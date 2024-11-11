package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrType
import com.decomposer.runtime.ir.Modality
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.IrBody
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.symbols.IrPropertySymbol
import com.decomposer.runtime.ir.symbols.IrSimpleFunctionSymbol

data class IrFunctionWithLateBinding(
    val isBound: Boolean,
    override val annotations: List<IrConstructorCall>,
    override val symbol: IrSimpleFunctionSymbol,
    override var overriddenSymbols: List<IrSimpleFunctionSymbol>,
    override var isTailrec: Boolean,
    override var isSuspend: Boolean,
    override var isOperator: Boolean,
    override var isInfix: Boolean,
    override var correspondingPropertySymbol: IrPropertySymbol?,
    override val isInline: Boolean,
    override val isExpect: Boolean,
    override val returnType: IrType,
    override val dispatchReceiverParameter: IrValueParameter?,
    override val extensionReceiverParameter: IrValueParameter?,
    override val contextReceiverParametersCount: Int,
    override val body: IrBody?,
    override val attributeMap: List<Any?>?,
    override var startOffset: Int,
    override var endOffset: Int,
    override val parent: IrDeclarationParent,
    override val isExternal: Boolean,
    override val name: Name,
    override val visibility: Visibility,
    override val typeParameters: List<IrTypeParameter>,
    override var isFakeOverride: Boolean,
    override var modality: Modality,
    override val attributeOwnerId: IrAttributeContainer,
    override val originalBeforeInline: IrAttributeContainer?
) : IrSimpleFunction()
