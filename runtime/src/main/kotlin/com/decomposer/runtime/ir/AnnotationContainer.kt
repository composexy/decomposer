package com.decomposer.runtime.ir

import com.decomposer.runtime.ir.expressions.ConstructorCall

interface AnnotationContainer {
    val annotations: List<ConstructorCall>
}
