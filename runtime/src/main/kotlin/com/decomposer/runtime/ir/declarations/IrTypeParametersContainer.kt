package com.decomposer.runtime.ir.declarations

interface IrTypeParametersContainer : IrDeclaration, IrDeclarationParent {
    val typeParameters: List<IrTypeParameter>
}
