package com.decomposer.sample.ir

fun breaks() {
    outerLoop@ for (i in 1..5) {
        for (j in 1..5) {
            if (j == 3) {
                println("Breaking out of outerLoop at i=$i, j=$j")
                break@outerLoop
            }
            println("i=$i, j=$j")
        }
    }

    for (i in 0 until 100) {
        if (i == 50) {
            break
        }
    }
}

fun typeOps() {
    val a = if ((System.currentTimeMillis() % 2).toInt() == 0) {
        B()
    } else {
        C()
    }
    val b = a as B
    val c = a as? C
    if (a is B) {
        println("Hello World!")
    }
}

interface Interface
private class C : Interface
private class B : Interface

fun throws() {
    throw IllegalArgumentException("Wrong call")
}

fun whiles() {
    var a = 0
    do {
        a++
    } while (a < 10)

    var b = 10
    while (b > 5) {
        b--
    }
}

fun returnLabel(numbers: List<Int>, target: Int): Boolean {
    numbers.forEach label@{
        if (it == target) {
            return@label
        }
    }
    return false
}

fun varargs(a: Int, b: String, vararg c: Int) {
    println("$a $b $c")
    val d = IntArray(12)
    varargs(10, "d", *d)
}
