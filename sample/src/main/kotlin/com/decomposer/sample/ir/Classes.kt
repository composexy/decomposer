package com.decomposer.sample.ir

typealias A = Map<String, Map<Int, String>>

enum class Lists {
    LIST1, LIST2, LIST3
}

enum class State(private val state: String) {
    GOOD("good"),
    BAD("bad");

    fun printState() {
        println("$state ${GOOD.state}")
    }
}

fun needState(state: State) {
    val data = DataClass("A", true)
    val real = RealType("real")
    if (state == State.BAD) {
        println("${Lists.LIST1} ${Lists.LIST3} $data ${real.id}")
    } else {
        Lists.entries.forEach {
            println("$it")
        }
    }
}

data class DataClass(
    val data1: String,
    val data2: Boolean = false
)

internal class ToString {
    override fun toString(): String {
        return "ToString"
    }

    operator fun get(index: Int): String {
        return "ToString ${Real.INDEX} ${Real.DEFAULT} $EmptyType"
    }
}

@JvmInline
value class MyLong(val long: Long)

interface MyInterface3<E> {
    fun run3()
}

private fun interface MyInterface {
    fun run()
}

interface MyInterface2 {
    fun run()
    fun run2()
}

abstract class MyBase<T>(val id: String, val value: T) : MyInterface2, MyInterface {
    abstract fun myBase(): T
}

class Real : MyBase<List<String>>(
    "myId", emptyList()
), MyInterface, MyInterface3<Map<String, List<Boolean>>> {
    override fun run2() {
        val prefix = "prefix"
        fun inner(): String {
            return "123"
        }
        val postfix = "postfix"
        println("$prefix ${inner()} $postfix")
    }

    override fun run() {
        run2()
    }

    override fun run3() {
        run()
    }

    override fun myBase(): List<String> {
        run()
        run3()
        return listOf("1", "2", INDEX.toString())
    }

    companion object {
        const val INDEX = 0
        val DEFAULT = Real()
    }

    inner class InnerClass(
        val index: Int
    )
}

sealed interface Type
data object EmptyType : Type
private class RealType(val id: String) : Type
