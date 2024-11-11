package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.ExternalPackageFragmentSymbol

data class ExternalPackageFragment(
    override val symbol: ExternalPackageFragmentSymbol,
    override val startOffset: Int,
    override val packageFqName: String,
    override val attributeMap: List<Any?>?,
    override val endOffset: Int,
    override val declarations: MutableList<Declaration>
) : PackageFragment()
