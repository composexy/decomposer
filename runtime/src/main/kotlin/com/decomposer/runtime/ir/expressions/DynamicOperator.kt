package com.decomposer.runtime.ir.expressions

import kotlinx.serialization.Serializable

@Serializable
enum class DynamicOperator(val image: String, val isAssignmentOperator: Boolean = false) {
    UNARY_PLUS("+"),
    UNARY_MINUS("-"),
    EXCL("!"),
    PREFIX_INCREMENT("++", isAssignmentOperator = true),
    POSTFIX_INCREMENT("++", isAssignmentOperator = true),
    PREFIX_DECREMENT("--", isAssignmentOperator = true),
    POSTFIX_DECREMENT("--", isAssignmentOperator = true),
    BINARY_PLUS("+"),
    BINARY_MINUS("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    GT(">"),
    LT("<"),
    GE(">="),
    LE("<="),
    EQEQ("=="),
    EXCLEQ("!="),
    EQEQEQ("==="),
    EXCLEQEQ("!=="),
    ANDAND("&&"),
    OROR("||"),
    EQ("=", isAssignmentOperator = true),
    PLUSEQ("+=", isAssignmentOperator = true),
    MINUSEQ("-=", isAssignmentOperator = true),
    MULEQ("*=", isAssignmentOperator = true),
    DIVEQ("/=", isAssignmentOperator = true),
    MODEQ("%=", isAssignmentOperator = true),
    ARRAY_ACCESS("[]"),
    INVOKE("()")
}
