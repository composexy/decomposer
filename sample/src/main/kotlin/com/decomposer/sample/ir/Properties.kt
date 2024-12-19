package com.decomposer.sample.ir

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView

val simpleProp = 12
private lateinit var lateVar: String
val withGetter: Int
    get() {
        return 123
    }

var withSetter: Int = 111
    get() {
        return field * 2
    }
    set(value) {
        field = value - 2
    }

@JvmField
var jvmField: String = "Hey"
var reference = ::withSetter.get()
var reference2 = Data::prop2
val data = Data(prop2 = false, value = "value")
val reference3 = data::prop3
val reference4 = Data.DataInner::prop6
val reference5 = data.inner::prop6
private val reference6 = data::prop3
val reference7 = data::prop8
val reference8 = data::prop9
var getterSetter: String = "World"
    get() {
        val value = field.length
        return "Hello $value $simpleProp"
    }
    set(value) {
        field = "$value ${Object1.two} ${Data.companion1}"
    }

object Object1 {
    private val one = "one"
    val two = "two"
    internal val three = "three"
    internal const val four = "four"
}

val annotatedAccessor: String
    @Composable
    get() {
        return LocalView.current.transitionName
    }

@get:SinceKotlin("1.3")
var annotatedAccessor2: String = ""
    get() {
        return "annotatedAccessor2"
    }
    set(value) {
        field = "$value $value"
    }

var initWithExpression: Long = System.currentTimeMillis() / 10

var propDelegated: Int by mutableIntStateOf(123)

class Data(
    val prop1: Int = Math.random().toInt(),
    val prop2: Boolean,
    value: String
) {
    val prop3: String = "Inner"
    val prop4: String
        get() = prop3::class.simpleName ?: ""
    val prop5: String by mutableStateOf("Hi")
    val prop6: String by lazy {
        "Hi"
    }
    val prop8: IntArray = IntArray(1)
    val prop9 = arrayOf(1, 3, 4)
    val prop10 = "$value $value"
    val inner: DataInner = DataInner("Hello")

    init {
        Log.w("Data", "constructing Data")
    }

    class DataInner(
        val prop6: String
    )

    fun accessProperties() {
        val prop7 = prop3 + prop4
        val reference = prop6::class
        val reference2 = prop6::class.simpleName
        val reference3 = ::prop4
        val prop8 = prop7 + prop1
    }

    companion object {
        const val companion1 = "companion1"
    }
}

class This(
    val value: String = "1234"
) {
    fun fetch(): String = "$value $this ${this.value}"
}
