package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.BlockBody
import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.AnonymousInitializerSymbol

data class AnonymousInitializer(
    var isStatic: Boolean,
    var body: BlockBody,
    override val symbol: AnonymousInitializerSymbol,
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<ConstructorCall>,
    override val attributeMap: List<Any?>?,
    override var parent: DeclarationParent
) : DeclarationBase
