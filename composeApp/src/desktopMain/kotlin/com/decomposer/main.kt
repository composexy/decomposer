package com.decomposer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import org.jetbrains.kotlin.backend.jvm.serialization.proto.JvmIr
import org.jetbrains.kotlin.metadata.jvm.deserialization.stringsToBytes
import org.jetbrains.kotlin.protobuf.CodedInputStream
import kotlin.time.Duration.Companion.seconds

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "decomposer",
    ) {
        adbReverse()
        App()
        setupWebSocket()
    }
}

fun adbReverse() {
    println("PATH ${System.getenv("PATH")}")
    Runtime.getRuntime().exec("adb reverse tcp:9900 tcp:9900")
}

fun setupWebSocket() {
    embeddedServer(Netty, port = 9900) {
        install(WebSockets) {
            pingPeriod = 15.seconds // Interval for sending ping frames (optional)
            timeout = 15.seconds // Connection timeout after no response (optional)
            maxFrameSize = Long.MAX_VALUE // Maximum frame size (optional)
            masking = false // Mask frames sent to the client (optional)
        }

        routing {
            webSocket("/") { // This defines the WebSocket route
                incoming.consumeEach { frame ->
                    when (frame) {
                        is Frame.Text -> {
                            val receivedText = frame.readText()
                            //println("Received $receivedText")
                            val array = arrayOf(receivedText)
                            val bytes = stringsToBytes(array)
                            val bytes2 = ByteArray(bytes.size - 1)
                            bytes.copyInto(bytes2, 0, 1)
                            val jvmIr = JvmIr.ClassOrFile.parseFrom(bytes2.codedInputStream)
                            jvmIr.stringList.forEach {
                                print("$it ")
                            }
                            jvmIr.debugInfoList.forEach {
                                print("$it ")
                            }
                        }
                        is Frame.Binary -> { }
                        else -> {
                            println("Unknown Received $frame")
                        }
                    }
                }
            }
        }
    }.start(wait = false)
}

val ByteArray.codedInputStream: CodedInputStream
    get() {
        val codedInputStream = CodedInputStream.newInstance(this)
        codedInputStream.setRecursionLimit(65535) // The default 64 is blatantly not enough for IR.
        return codedInputStream
    }