package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrStatement

interface IrDeclaration : IrStatement, IrSymbolOwner, IrMutableAnnotationContainer {
    val parent: IrDeclarationParent
}
