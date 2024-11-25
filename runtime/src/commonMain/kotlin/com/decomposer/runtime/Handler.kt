package com.decomposer.runtime

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.serialization.Serializable

@Serializable
data class Command(
    val key: String,
    val parameters: List<String> = emptyList()
)

object CommandKeys {
    const val PROJECT_SNAPSHOT = "PROJECT_SNAPSHOT"
    const val VIRTUAL_FILE_IR = "VIRTUAL_FILE_IR"
    const val COMPOSITION_DATA = "COMPOSITION_DATA"
}

internal interface CommandHandler {
    val expectedKey: String
    suspend fun DefaultClientWebSocketSession.processCommand(command: Command)
}
