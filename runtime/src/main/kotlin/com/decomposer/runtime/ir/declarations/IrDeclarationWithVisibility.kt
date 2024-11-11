package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Visibility

interface IrDeclarationWithVisibility : IrDeclaration {
    val visibility: Visibility
}
