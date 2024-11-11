package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.FileSymbol

data class File(
    override val symbol: FileSymbol,
    val module: Module,
    val fileEntry: FileEntry,
    override val attributeMap: List<Any?>?,
    override var packageFqName: String,
    override val startOffset: Int,
    override val endOffset: Int,
    override val declarations: MutableList<Declaration>,
    override val annotations: List<ConstructorCall>
) : PackageFragment(), MutableAnnotationContainer, MetadataSourceOwner
