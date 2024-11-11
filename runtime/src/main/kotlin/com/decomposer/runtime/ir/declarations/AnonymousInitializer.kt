package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.BlockBody
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.AnonymousInitializerSymbol
import kotlinx.serialization.Serializable

@Serializable
data class AnonymousInitializer(
    var isStatic: Boolean,
    var body: BlockBody,
    override val symbol: AnonymousInitializerSymbol,
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<ConstructorCall>,
    override var parent: DeclarationParent
) : DeclarationBase
