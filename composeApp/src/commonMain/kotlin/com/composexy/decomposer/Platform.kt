package com.composexy.decomposer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform