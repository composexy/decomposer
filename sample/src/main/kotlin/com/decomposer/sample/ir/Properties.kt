package com.decomposer.sample.ir

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
/*
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

var reference = ::withSetter.get()*/
var reference2 = Data::prop2
val data = Data(prop2 = false)
val reference3 = data::prop3
val reference4 = Data.DataInner::prop6

var getNoBackingField: String = ""
    get() {
        return "Hello"
    }
    set(value) {
        field = ""
    }

var initWithExpression: Long = System.currentTimeMillis() / 10

var propDelegated: Int by mutableIntStateOf(123)

data class Data(
    val prop1: Int = Math.random().toInt(),
    val prop2: Boolean
) {
    val prop3: String = "Inner"
    val prop4: String
        get() = prop3::class.simpleName ?: ""
    val prop5: String by mutableStateOf("Hi")
    val prop6: String by lazy {
        "Hi"
    }

    class DataInner(
        val prop6: String
    )
}
