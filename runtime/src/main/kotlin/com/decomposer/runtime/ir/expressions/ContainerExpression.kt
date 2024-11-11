package com.decomposer.runtime.ir.expressions

import kotlinx.serialization.Serializable

@Serializable
abstract class ContainerExpression : Expression(), StatementContainer {
    abstract val origin: StatementOrigin?
}
