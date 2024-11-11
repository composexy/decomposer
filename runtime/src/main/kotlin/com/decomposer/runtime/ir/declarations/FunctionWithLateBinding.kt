package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.Modality
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.Body
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.PropertySymbol
import com.decomposer.runtime.ir.symbols.SimpleFunctionSymbol
import kotlinx.serialization.Serializable

@Serializable
data class FunctionWithLateBinding(
    val isBound: Boolean,
    override val annotations: List<ConstructorCall>,
    override val symbol: SimpleFunctionSymbol,
    override var overriddenSymbols: List<SimpleFunctionSymbol>,
    override var isTailrec: Boolean,
    override var isSuspend: Boolean,
    override var isOperator: Boolean,
    override var isInfix: Boolean,
    override var correspondingPropertySymbol: PropertySymbol?,
    override val isInline: Boolean,
    override val isExpect: Boolean,
    override val returnType: Type,
    override val dispatchReceiverParameter: ValueParameter?,
    override val extensionReceiverParameter: ValueParameter?,
    override val contextReceiverParametersCount: Int,
    override val body: Body?,
    override var startOffset: Int,
    override var endOffset: Int,
    override val parent: DeclarationParent,
    override val isExternal: Boolean,
    override val name: Name,
    override val visibility: Visibility,
    override val typeParameters: List<TypeParameter>,
    override var isFakeOverride: Boolean,
    override var modality: Modality,
    override val attributeOwnerId: AttributeContainer,
    override val originalBeforeInline: AttributeContainer?
) : SimpleFunction()
