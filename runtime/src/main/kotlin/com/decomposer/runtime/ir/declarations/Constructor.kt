package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Type
import com.decomposer.runtime.ir.Name
import com.decomposer.runtime.ir.Visibility
import com.decomposer.runtime.ir.expressions.Body
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.ConstructorSymbol
import kotlinx.serialization.Serializable

@Serializable
data class Constructor(
    override val symbol: ConstructorSymbol,
    val isPrimary: Boolean,
    override val annotations: List<ConstructorCall>,
    override val isInline: Boolean,
    override val isExpect: Boolean,
    override val returnType: Type,
    override val dispatchReceiverParameter: ValueParameter?,
    override val extensionReceiverParameter: ValueParameter?,
    override val contextReceiverParametersCount: Int,
    override val body: Body?,
    override val startOffset: Int,
    override val endOffset: Int,
    override val parent: DeclarationParent,
    override val isExternal: Boolean,
    override val name: Name,
    override val visibility: Visibility,
    override val typeParameters: List<TypeParameter>
) : Function()
