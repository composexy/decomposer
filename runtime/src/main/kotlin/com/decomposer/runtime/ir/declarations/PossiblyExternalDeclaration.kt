package com.decomposer.runtime.ir.declarations

interface PossiblyExternalDeclaration : DeclarationWithName {
    val isExternal: Boolean
}
