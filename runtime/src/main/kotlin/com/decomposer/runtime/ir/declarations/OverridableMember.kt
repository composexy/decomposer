package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Modality
import kotlinx.serialization.Serializable

@Serializable
sealed interface OverridableMember : Declaration, DeclarationWithVisibility,
    DeclarationWithName, SymbolOwner {
    val modality: Modality
}
