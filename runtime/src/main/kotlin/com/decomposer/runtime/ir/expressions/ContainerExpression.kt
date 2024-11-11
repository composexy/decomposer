package com.decomposer.runtime.ir.expressions

abstract class ContainerExpression : Expression(), StatementContainer {
    abstract val origin: StatementOrigin?
}
