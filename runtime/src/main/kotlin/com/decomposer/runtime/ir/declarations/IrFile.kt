package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.symbols.IrFileSymbol

abstract class IrFile : IrPackageFragment(), IrMutableAnnotationContainer, IrMetadataSourceOwner {
    abstract override val symbol: IrFileSymbol
    abstract var module: IrModule
    abstract var fileEntry: IrFileEntry
}
