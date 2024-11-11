package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Name

interface DeclarationWithName : Declaration {
    val name: Name
}
