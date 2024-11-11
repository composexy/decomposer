package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.ElementBase
import com.decomposer.runtime.ir.symbols.PackageFragmentSymbol

abstract class PackageFragment : ElementBase, DeclarationContainer, SymbolOwner {
    abstract override val symbol: PackageFragmentSymbol
    abstract val packageFqName: String
}
