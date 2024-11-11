package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Modality

sealed interface OverridableMember : Declaration, DeclarationWithVisibility,
    DeclarationWithName, SymbolOwner {
    val modality: Modality
}
