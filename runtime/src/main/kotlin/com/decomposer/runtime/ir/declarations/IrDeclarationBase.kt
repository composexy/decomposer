package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.IrElementBase

abstract class IrDeclarationBase : IrElementBase(), IrDeclaration {
    final override lateinit var parent: IrDeclarationParent
}
