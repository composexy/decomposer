package com.decomposer.runtime.connection

import com.decomposer.runtime.Command
import com.decomposer.runtime.CommandHandler
import com.decomposer.runtime.CommandKeys
import com.decomposer.runtime.connection.model.ProjectSnapshot
import com.decomposer.runtime.connection.model.VirtualFileIr
import com.decomposer.runtime.ir.ProjectScanner
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized

internal class ProjectSnapshotHandler(
    private val projectScanner: ProjectScanner
) : CommandHandler {
    override val expectedKey = CommandKeys.PROJECT_SNAPSHOT

    override suspend fun DefaultClientWebSocketSession.processCommand(command: Command) {
        assert(expectedKey == command.key)
        val projectSnapshot = ProjectSnapshot(projectScanner.fetchProjectSnapshot())
        sendSerialized(projectSnapshot)
    }
}

internal class VirtualFileIrHandler(
    private val projectScanner: ProjectScanner
) : CommandHandler {
    override val expectedKey = CommandKeys.VIRTUAL_FILE_IR

    override suspend fun DefaultClientWebSocketSession.processCommand(command: Command) {
        assert(expectedKey == command.key)
        val filePaths = command.parameters
        filePaths.forEach {
            val virtualFileIr = projectScanner.fetchIr(it)
            sendSerialized(
                VirtualFileIr(
                    filePath = it,
                    composedIrFile = virtualFileIr.composedIrFile ?: emptyList(),
                    composedTopLevelIrClasses = virtualFileIr.composedTopLevelIrClasses,
                    originalIrFile = virtualFileIr.originalIrFile ?: emptyList(),
                    originalTopLevelIrClasses = virtualFileIr.originalTopLevelIrClasses
                )
            )
        }
    }
}
