package com.decomposer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform