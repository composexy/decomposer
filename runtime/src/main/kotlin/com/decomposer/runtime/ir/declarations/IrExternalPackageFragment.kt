package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.IrExternalPackageFragmentSymbol

abstract class IrExternalPackageFragment : IrPackageFragment() {
    abstract override val symbol: IrExternalPackageFragmentSymbol
}
