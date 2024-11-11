package com.decomposer.runtime.ir.declarations

interface IrPossiblyExternalDeclaration : IrDeclarationWithName {
    var isExternal: Boolean
}
