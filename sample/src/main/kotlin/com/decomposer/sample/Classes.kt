package com.decomposer.sample

data class Name(
    val firstName: String,
    val lastName: String
)

data class Age(
    var currentAge: Int
)

typealias MyList<T> = Map<String, T>

val list: MyList<Int>? = null