package com.decomposer.server

import java.io.BufferedReader
import java.io.InputStreamReader

class AdbConnection(private val port: Int) {

    fun connect(): AdbConnectResult {
        val processBuilder = ProcessBuilder("adb", "reverse", "tcp:$port", "tcp:$port")
        try {
            val process = processBuilder.start()
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                val errorMessage = errorReader.use {
                    buildString {
                        val lines = errorReader.readLines()
                        lines.forEach { append(it) }
                    }
                }
                return AdbConnectResult.Failure(errorMessage)
            } else {
                return AdbConnectResult.Success
            }
        } catch (ex: Exception) {
            return AdbConnectResult.Failure(
                ex.message ?: ex.stackTraceToString().lines().first()
            )
        }
    }
}

sealed interface AdbConnectResult {

    data object Idle : AdbConnectResult

    data object Success : AdbConnectResult

    data object Skipped : AdbConnectResult

    data class Failure(val errorMessage: String) : AdbConnectResult
}
