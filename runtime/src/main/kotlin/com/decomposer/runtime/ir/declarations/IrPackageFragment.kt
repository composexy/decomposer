package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrElementBase
import com.decomposer.runtime.ir.symbols.IrPackageFragmentSymbol

abstract class IrPackageFragment : IrElementBase, IrDeclarationContainer, IrSymbolOwner {
    abstract override val symbol: IrPackageFragmentSymbol
    abstract val packageFqName: String
}
