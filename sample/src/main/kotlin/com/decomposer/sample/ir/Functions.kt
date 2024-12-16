package com.decomposer.sample.ir

fun empty() { }

fun singleParameter(input: Int) { }

val time = System.currentTimeMillis()

private fun String.indented() = "\t$this"

@PublishedApi
internal inline fun <reified T, R : Printer, F : List<*>> generics(input: T): String {
    if (T::class == String::class) {
        val hello = "Hello at $time"
        return hello
    } else {
        return "Hello world"
    }
}

private inline fun inlined(block: Printer.() -> Unit) {
    Child().block()
}

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
    private fun String.spaced() = " $this "
}

class Constructors private constructor(
    val id: String,
    val isAdmin: Boolean
) {
    private constructor(id: String) : this(id, id == "decomposer")
}
