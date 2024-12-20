package com.decomposer.sample.ir

import androidx.compose.runtime.Composable

fun empty() { }
fun empty2() = Unit

var outer = 10

fun nullable(): String? {
    outer = 15
    var random = Math.random().toInt()
    random += outer
    var variable = 21L
    variable = (outer + random).toLong()
    return if (random == 0) "Hi" else null
}

fun annotated(): @Composable ((Int, String?, Map<String, *>?) -> Boolean)? {
    return { i, _, m -> false }
}

fun singleParameter(input: Int) { }

@SinceKotlin("1.3")
fun <T : List<T>> complexParameters(
    input1: String? = "Dave",
    input2: MutableMap<*, *>? = myMap(),
    input3: @Composable String.(@Composable Int.() -> Boolean) -> Unit,
    input4: String.() -> Unit = if (input1 == "Dave") {
        { }
    } else {
        { println("Non default") }
    }
): Boolean? {
    return false
}

private fun myMap(): MutableMap<Int, List<String>> = mutableMapOf()

val time = System.currentTimeMillis()

private fun String.indented() = "\t$this"

@PublishedApi
internal inline fun <reified T, R : Printer, F : List<*>> generics(
    input: T,
    input2: R,
    input3: F
): String {
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
    fun print(string: String)
}

abstract class Base(val base: String, time: Long) : Printer {
    private val time = time.toString()
    abstract fun base()
    open fun open() {
        print("$base $time")
    }
    open fun open2() {}
    open fun open3() {}
}

class Child : Base("Child", System.currentTimeMillis()) {
    override fun base() {}
    override fun open() {}
    override fun open2() {
        super.open2()
    }
    override fun print(string: String) {}
    private fun String.spaced() = " $this "
}

class Constructors private constructor(
    val id: String,
    val isAdmin: Boolean
) {
    private constructor(id: String) : this(id, id == "decomposer")
}

class NoPrimary {
    constructor(id: String)
}
