package com.decomposer.runtime.ir.declarations

interface TypeParametersContainer : Declaration, DeclarationParent {
    val typeParameters: List<TypeParameter>
}
