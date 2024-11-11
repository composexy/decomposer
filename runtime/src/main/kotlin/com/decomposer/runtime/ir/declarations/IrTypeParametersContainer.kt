package com.decomposer.runtime.ir.declarations

interface IrTypeParametersContainer : IrDeclaration, IrDeclarationParent {
    var typeParameters: List<IrTypeParameter>
}
