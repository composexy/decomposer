package com.decomposer.runtime.ir.declarations

interface IrPossiblyExternalDeclaration : IrDeclarationWithName {
    val isExternal: Boolean
}
