package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Visibility

interface DeclarationWithVisibility : Declaration {
    val visibility: Visibility
}
