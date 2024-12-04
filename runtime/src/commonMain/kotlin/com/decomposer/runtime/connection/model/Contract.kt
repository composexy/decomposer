package com.decomposer.runtime.connection.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.subclass

@Serializable
data class Command(
    val key: String,
    val parameters: List<String> = emptyList()
)

@Serializable
sealed class CommandResponse

@Serializable
class ProjectSnapshotResponse(
    val projectSnapshot: ProjectSnapshot
) : CommandResponse()

@Serializable
class VirtualFileIrResponse(
    val virtualFileIr: VirtualFileIr
) : CommandResponse()

@Serializable
class CompositionDataResponse(
    val compositionRoots: CompositionRoots
) : CommandResponse()

val commandResponseSerializer = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        polymorphic(CommandResponse::class) {
            subclass(ProjectSnapshotResponse::class)
            subclass(VirtualFileIrResponse::class)
            subclass(CompositionDataResponse::class)
        }
    }
}

object CommandKeys {
    const val PROJECT_SNAPSHOT = "PROJECT_SNAPSHOT"
    const val VIRTUAL_FILE_IR = "VIRTUAL_FILE_IR"
    const val COMPOSITION_DATA = "COMPOSITION_DATA"
}
