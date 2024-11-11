package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Name

interface IrDeclarationWithName : IrDeclaration {
    var name: Name
}
