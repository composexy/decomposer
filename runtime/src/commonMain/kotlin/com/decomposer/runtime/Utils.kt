package com.decomposer.runtime

fun interface Logger {
    fun log(level: Level, tag: String, message: String)

    enum class Level {
        DEBUG, INFO, WARNING, ERROR
    }
}
