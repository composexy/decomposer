package com.decomposer.runtime.connection

import com.decomposer.runtime.Logger
import com.decomposer.runtime.compose.CompositionNormalizer
import com.decomposer.runtime.connection.model.Command
import com.decomposer.runtime.connection.model.CommandKeys
import com.decomposer.runtime.connection.model.CommandResponse
import com.decomposer.runtime.connection.model.CompositionDataResponse
import com.decomposer.runtime.connection.model.ProjectSnapshot
import com.decomposer.runtime.connection.model.ProjectSnapshotResponse
import com.decomposer.runtime.connection.model.SessionData
import com.decomposer.runtime.connection.model.VirtualFileIr
import com.decomposer.runtime.connection.model.VirtualFileIrResponse
import com.decomposer.runtime.ir.ProjectScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

internal abstract class AbstractOkHttpClient(
    private val serverPort: Int,
    private val projectScanner: ProjectScanner,
    private val compositionNormalizer: CompositionNormalizer
) : Logger, Client {

    private lateinit var webSocket: WebSocket
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val loggerTag = this::class.java.simpleName
    private val okHttpClient = OkHttpClient()

    override fun start() {
        val device = buildDeviceDescriptor()
        val newSessionRequest = Request.Builder().url(
            "http://localhost:$serverPort/${ConnectionContract.DEFAULT_CONNECTION_PATH}"
        ).header(ConnectionContract.HEADER_DEVICE_TYPE, device.deviceType.name).build()

        okHttpClient.newCall(newSessionRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                log(Logger.Level.WARNING, loggerTag, e.stackTraceToString())
                restart()
            }

            override fun onResponse(call: Call, response: Response) {
                when (val statusCode = response.code) {
                    200 -> {
                        val body = response.body?.string()
                        log(Logger.Level.DEBUG, loggerTag, "Received sessionData: $body")
                        if (body == null) {
                            log(Logger.Level.ERROR, loggerTag, "Unexpected empty body")
                            restart()
                            return
                        }
                        try {
                            val sessionData = Json.decodeFromString<SessionData>(body)
                            webSocket = runSession(sessionData.sessionUrl)
                        } catch (ex: Exception) {
                            log(Logger.Level.ERROR, loggerTag, "Unexpected error while parsing body: ${ex.stackTraceToString()}")
                            restart()
                        }
                    }
                    else -> {
                        log(Logger.Level.INFO, loggerTag, "Unexpected status code: $statusCode")
                        restart()
                    }
                }
            }
        })
    }

    private fun restart() {
        coroutineScope.launch {
            delay(PROBE_INTERVAL_SECONDS.seconds)
            start()
        }
    }

    override fun stop() {
        webSocket.close(1000, "stop")
        coroutineScope.cancel()
    }

    private fun runSession(sessionUrl: String): WebSocket {
        val websocketRequest = Request.Builder()
            .url("ws://localhost:$serverPort/$sessionUrl")
            .build()
        val websocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                log(Logger.Level.DEBUG, loggerTag, "Websocket onOpen")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                log(Logger.Level.DEBUG, loggerTag, "Received onMessage text: $text")
                val command = Json.decodeFromString<Command>(text)
                when (command.key) {
                    CommandKeys.VIRTUAL_FILE_IR -> {
                        val filePaths = command.parameters
                        processVirtualFileIr(webSocket, filePaths)
                    }
                    CommandKeys.PROJECT_SNAPSHOT -> processProjectSnapshot(webSocket)
                    CommandKeys.COMPOSITION_DATA -> processCompositionData(webSocket)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                log(Logger.Level.DEBUG, loggerTag, "Received onMessage bytes: $bytes")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                log(Logger.Level.DEBUG, loggerTag, "Websocket onClosing $code $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                log(Logger.Level.DEBUG, loggerTag, "Websocket onClosed $code $reason")
                restart()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                log(Logger.Level.WARNING, loggerTag, "Websocket onFailure: ${t.stackTraceToString()}")
                restart()
            }
        }
        return okHttpClient.newWebSocket(websocketRequest, websocketListener)
    }

    private fun processCompositionData(webSocket: WebSocket) {
        coroutineScope.launch {
            val compositionRoots = compositionNormalizer.extractCompositionRoots()
            val response = CompositionDataResponse(compositionRoots)
            val serialized = Json.encodeToString(CommandResponse.serializer(), response)
            webSocket.send(serialized)
        }
    }

    private fun processProjectSnapshot(webSocket: WebSocket) {
        coroutineScope.launch {
            val scannedResult = projectScanner.fetchProjectSnapshot()
            val projectSnapshot = ProjectSnapshot(
                fileTree = scannedResult.first,
                packagesByPath = scannedResult.second
            )
            val response = ProjectSnapshotResponse(projectSnapshot)
            val serialized = Json.encodeToString(CommandResponse.serializer(), response)
            webSocket.send(serialized)
        }
    }

    private fun processVirtualFileIr(webSocket: WebSocket, filePaths: List<String>) {
        coroutineScope.launch {
            filePaths.forEach {
                val ir = projectScanner.fetchIr(it)
                val virtualFileIr = VirtualFileIr(
                    filePath = it,
                    composedIrFile = ir.composedIrFile ?: emptyList(),
                    composedTopLevelIrClasses = ir.composedTopLevelIrClasses,
                    composedStandardDump = ir.composedStandardDump,
                    originalIrFile = ir.originalIrFile ?: emptyList(),
                    originalTopLevelIrClasses = ir.originalTopLevelIrClasses,
                    originalStandardDump = ir.originalStandardDump
                )
                val response = VirtualFileIrResponse(virtualFileIr)
                val serialized = Json.encodeToString(CommandResponse.serializer(), response)
                webSocket.send(serialized)
            }
        }
    }

    companion object {
        private const val PROBE_INTERVAL_SECONDS = 3L
    }
}
