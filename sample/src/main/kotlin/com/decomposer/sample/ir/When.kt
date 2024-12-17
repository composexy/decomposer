package com.decomposer.sample.ir

val val1 = System.currentTimeMillis()
val val2 = System.nanoTime()
var var3 = "Hey"
var var4 = "Hello"
var var5 = true
var var6 = false

var good = var5 && var6
var lucky = val1.toInt() % 4 == 1 || val2.toInt() % 3 == 2
var notLucky = val1 > 100 || val2 == 1000L && var3.length > 34 || var4.startsWith("He")

var funny = if (var3.startsWith("Lucy")) {
    100
} else if (var4.length > 11) {
    1000
} else {
    1010
}

val simple = when (val1) {
    1234L -> "A"
    1231L -> "B"
    10003L -> "C"
    else -> "D"
}

val complex = when {
    val1 != 10394L -> 1234
    var4.startsWith("A") -> 3133
    else -> 1355
}
