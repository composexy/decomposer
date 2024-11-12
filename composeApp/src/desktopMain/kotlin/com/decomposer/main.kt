package com.decomposer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.decomposer.runtime.SerializedIrFile
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
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.backend.common.serialization.IrFileSerializer
import org.jetbrains.kotlin.backend.common.serialization.proto.IdSignature
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclaration
import org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression
import org.jetbrains.kotlin.backend.common.serialization.proto.IrFile
import org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement
import org.jetbrains.kotlin.backend.common.serialization.proto.IrType
import org.jetbrains.kotlin.library.SerializedDeclaration
import org.jetbrains.kotlin.library.encodings.WobblyTF8
import org.jetbrains.kotlin.metadata.jvm.deserialization.BitEncoding
import org.jetbrains.kotlin.protobuf.CodedInputStream
import java.io.DataInputStream
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
                            /*
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
                            */
                            val serializedIrFile = Json.decodeFromString<SerializedIrFile>(receivedText)

                            val fileData = serializedIrFile.fileData
                            val fqName = serializedIrFile.fqName
                            val path = serializedIrFile.path
                            val types = serializedIrFile.types
                            val signatures = serializedIrFile.signatures
                            val strings = serializedIrFile.strings
                            val bodies = serializedIrFile.bodies
                            val declarations = serializedIrFile.declarations
                            val debugInfo = serializedIrFile.debugInfo
                            val backendSpecificMetadata = serializedIrFile.backendSpecificMetadata

                            val irFile = IrFile.parseFrom(BitEncoding.decodeBytes(fileData))
                            println("$irFile")
                            println("fqName $fqName path $path")
                            val irTypes = readMemoryArray(BitEncoding.decodeBytes(types)) {
                                IrType.parseFrom(it)
                            }
                            irTypes.forEach {
                                println("irType ${it.simple.classifier}")
                            }
                            val irSignatures = readMemoryArray(BitEncoding.decodeBytes(signatures)) {
                                IdSignature.parseFrom(it)
                            }
                            irSignatures.forEach {
                                println("irSignature $it")
                            }
                            val irStrings = readMemoryString(BitEncoding.decodeBytes(strings))
                            irStrings.forEach {
                                println("irString $it")
                            }
                            val irBodies = readMemoryArray(BitEncoding.decodeBytes(bodies)) {
                                var isExpression = true
                                var isStatement = true
                                try {
                                    val express = IrExpression.parseFrom(it)
                                    println("expression ${express.type} ${express.operation} ${express.coordinates}")
                                } catch (e: Exception) {
                                    isExpression = false
                                }
                                if (!isExpression) {
                                    try {
                                        val stats = IrStatement.parseFrom(it)
                                        println("statement ${stats.coordinates} ${stats.statementCase.name} ${stats.statementCase.number}")
                                    } catch (e: Exception) {
                                        isStatement = false
                                    }
                                }
                                it
                            }
                            val irDeclarations = readMemoryDeclaration(BitEncoding.decodeBytes(declarations))
                            irDeclarations.forEach {
                                println("irDeclaration ${it.irClass} ${it.irFunction}")
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

fun <R> readMemoryArray(byteArray: ByteArray, block: (ByteArray) -> R): List<R> {
    val input = DataInputStream(byteArray.inputStream())
    val size = input.readInt()
    val lengths = IntArray(size)
    for (i in 0 until  size) {
        lengths[i] = input.readInt()
    }
    val byteArrays = mutableListOf<ByteArray>()
    for (i in 0 until  size) {
        byteArrays.add(ByteArray(lengths[i]))
    }
    for (i in 0 until  size) {
        input.read(byteArrays[i])
    }
    return byteArrays.map {
        block(it)
    }
}

fun readMemoryString(byteArray: ByteArray): List<String> {
    val input = DataInputStream(byteArray.inputStream())
    val size = input.readInt()
    val lengths = IntArray(size)
    for (i in 0 until  size) {
        lengths[i] = input.readInt()
    }
    val byteArrays = mutableListOf<ByteArray>()
    for (i in 0 until  size) {
        byteArrays.add(ByteArray(lengths[i]))
    }
    for (i in 0 until  size) {
        input.read(byteArrays[i])
    }
    return byteArrays.map {
        WobblyTF8.decode(it)
    }
}

private val SINGLE_INDEX_RECORD_SIZE = 3 * Int.SIZE_BYTES
private val INDEX_HEADER_SIZE = Int.SIZE_BYTES

fun readMemoryDeclaration(byteArray: ByteArray): List<IrDeclaration> {
    val input = DataInputStream(byteArray.inputStream())
    val size = input.readInt()

    val ids = IntArray(size)
    val offsets = IntArray(size)
    val sizes = IntArray(size)

    for (i in 0 until  size) {
        ids[i] = input.readInt()
        offsets[i] = input.readInt()
        sizes[i] = input.readInt()
    }

    val byteArrays = mutableListOf<ByteArray>()
    for (i in 0 until  size) {
        byteArrays.add(ByteArray(sizes[i]))
    }
    for (i in 0 until  size) {
        input.read(byteArrays[i])
    }
    return byteArrays.map {
        IrDeclaration.parseFrom(it)
    }
}