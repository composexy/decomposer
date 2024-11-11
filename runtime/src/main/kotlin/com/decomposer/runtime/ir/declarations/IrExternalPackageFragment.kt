package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.IrExternalPackageFragmentSymbol

data class IrExternalPackageFragment(
    override val symbol: IrExternalPackageFragmentSymbol,
    override val startOffset: Int,
    override val packageFqName: String,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val declarations: MutableList<IrDeclaration>
) : IrPackageFragment()
