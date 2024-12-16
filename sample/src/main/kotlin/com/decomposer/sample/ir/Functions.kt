package com.decomposer.sample.ir

fun empty() { }

fun singleParameter(input: Int) { }

fun interface Printer {
    fun print()
}

abstract class Base : Printer {
    abstract fun base()
    open fun open() {}
    open fun open2() {}
    open fun open3() {}
}

class Child : Base() {
    override fun base() {}
    override fun open() {}
    override fun open2() {
        super.open2()
    }
    override fun print() {}
}
