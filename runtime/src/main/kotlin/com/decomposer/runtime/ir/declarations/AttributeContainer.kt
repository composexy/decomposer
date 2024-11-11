package com.decomposer.runtime.ir.declarations

import com.decomposer.runtime.ir.Element

interface AttributeContainer : Element {
    val attributeOwnerId: AttributeContainer
    val originalBeforeInline: AttributeContainer?
}
