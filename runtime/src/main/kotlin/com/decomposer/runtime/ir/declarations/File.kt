package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.expressions.ConstructorCall
import com.decomposer.runtime.ir.symbols.FileSymbol
import kotlinx.serialization.Serializable

@Serializable
data class File(
    override val symbol: FileSymbol,
    val module: Module,
    val fileEntry: FileEntry,
    override var packageFqName: String,
    override val startOffset: Int,
    override val endOffset: Int,
    override val declarations: List<Declaration>,
    override val annotations: List<ConstructorCall>
) : PackageFragment(), MutableAnnotationContainer, MetadataSourceOwner
