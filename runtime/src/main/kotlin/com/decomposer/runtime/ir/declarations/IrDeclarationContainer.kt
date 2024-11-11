package com.decomposer.runtime.ir.declarations

interface IrDeclarationContainer : IrDeclarationParent {
    val declarations: MutableList<IrDeclaration>
}
