package com.decomposer.sample

class Person2(val name: String, val age: Int) {
    constructor(name: String) : this(name, 0)
}

data class Name(
    val firstName: String,
    val lastName: String
)

data class Age(
    var currentAge: Int
)

typealias MyList<T> = Map<String, T>

val list: MyList<Int>? = null