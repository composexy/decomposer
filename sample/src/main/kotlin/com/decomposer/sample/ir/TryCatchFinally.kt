package com.decomposer.sample.ir

class TryCatchFinally {

    fun test() {
        val a = try {
            val b = "1234"
            b.toShort()
        } catch (ex: NumberFormatException) {
            println(ex.stackTraceToString())
        } catch (ex: ConcurrentModificationException) {
            println("ConcurrentModificationException")
        } finally {
            println("Success!")
        }
    }

    inline fun withTry(scope: String, block: String.(String) -> Unit) {
        try {
            println(scope)
            scope.block(scope)
        } catch (ex: Exception) {
            println("Exception")
        } finally {
            println("finally")
        }
    }
}
