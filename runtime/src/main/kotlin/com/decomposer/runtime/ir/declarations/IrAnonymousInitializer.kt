package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.IrBlockBody
import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.symbols.IrAnonymousInitializerSymbol

data class IrAnonymousInitializer(
    var isStatic: Boolean,
    var body: IrBlockBody,
    override val symbol: IrAnonymousInitializerSymbol,
    override val startOffset: Int,
    override val endOffset: Int,
    override var annotations: List<IrConstructorCall>,
    override val attributeMap: List<Any?>?,
    override var parent: IrDeclarationParent
) : IrDeclarationBase
