package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Modality

sealed interface IrOverridableMember : IrDeclaration, IrDeclarationWithVisibility,
    IrDeclarationWithName, IrSymbolOwner {
    val modality: Modality
}
