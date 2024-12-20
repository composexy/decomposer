package com.decomposer.sample.ir

var a = 12
val b = arrayOf(1, 2, 3)
val c = b[1]
val d = a++
val e = a--
val f = a == c
val g = a != c
val h = a === c
val i = 12 in b
val j = 12 !in b
val k = (d + 9 - a) * 9 / 4
val l = (a + 2 > 8 || d <= 11 && a + 4 < 15 || a * 9 <= 11) && g
var m = if (a != 11) {
    13
} else 15
var n = a !== 8
var o = -a
var p = --a
var q = +a
var r = ++a
val s = 1 to 10
val t = 1 until 100
var u = 10 % 2
var w = 1 .. 11
val x = mapOf("1" to 12, "12" to 11)
var y = x["123"] ?: x["1"] ?: "11"
var z = a and c
val aa = z or z
var ab = z xor z
var ac = z shl 2
var ad = z shr 1
var ae = z ushr 2
var af = x["12"]

fun test() {
    u = a
    u += 1
    u -= a
    u *= e
    u /= o
    u %= 2
}
