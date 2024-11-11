package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.IrConstructorCall
import com.decomposer.runtime.ir.symbols.IrFileSymbol

data class IrFile(
    override val symbol: IrFileSymbol,
    val module: IrModule,
    val fileEntry: IrFileEntry,
    override val attributeMap: List<Any?>?,
    override var packageFqName: String,
    override val startOffset: Int,
    override val endOffset: Int,
    override val declarations: MutableList<IrDeclaration>,
    override val annotations: List<IrConstructorCall>
) : IrPackageFragment(), IrMutableAnnotationContainer, IrMetadataSourceOwner
