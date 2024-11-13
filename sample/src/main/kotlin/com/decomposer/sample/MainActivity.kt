package com.decomposer.sample

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import com.decomposer.runtime.SerializedIrFile
import com.decomposer.sample.proto.IdSignature
import com.decomposer.sample.proto.IrDeclaration
import com.decomposer.sample.proto.IrExpression
import com.decomposer.sample.proto.IrFile
import com.decomposer.sample.proto.IrStatement
import com.decomposer.sample.proto.IrType
import com.google.protobuf.util.JsonFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.jetbrains.kotlin.library.encodings.WobblyTF8
import org.jetbrains.kotlin.metadata.jvm.deserialization.BitEncoding
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.ValueType
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.value.ArrayEncodedValue
import org.jf.dexlib2.iface.value.EncodedValue
import org.jf.dexlib2.iface.value.StringEncodedValue
import java.io.DataInputStream
import java.io.File
import java.util.zip.ZipFile

class MainActivity : ComponentActivity() {

    private lateinit var webSocket: WebSocket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webSocket = start()
        enableEdgeToEdge()
        setContent {
            Empty()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            listAllClassesInApk4(this@MainActivity)
        }
    }

    private val client = OkHttpClient()

    fun listAllClassesInApk4(context: Context) {
        // Get the APK path
        val apkPath = context.applicationInfo.sourceDir
        val apkFile = File(apkPath)

        // Directory for storing temporary dex files
        val tempDexDir = context.getDir("dex", Context.MODE_PRIVATE)

        try {
            // Open APK as a ZipFile
            val zipFile = ZipFile(apkFile)

            // Process each dex file
            val dexEntries = zipFile.entries().asSequence()
                .filter { it.name.endsWith(".dex") }
                .toList()

            for (dexEntry in dexEntries) {
                // Extract dex file to a temporary file
                //Log.e("Test", dexEntry.name)
                val tempDexFile = File(tempDexDir, dexEntry.name)
                zipFile.getInputStream(dexEntry).use { input ->
                    tempDexFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Load the dex file with Dexlib2 and list classes
                val dexFile = DexFileFactory.loadDexFile(tempDexFile, Opcodes.forApi(Build.VERSION.SDK_INT)) // API 19 as baseline
                //Log.e("Test", "classes ${dexFile.classes.size}")
                dexFile.classes.forEach { classDef: ClassDef ->
                    if (classDef.type.contains("decomposer")) {
                        classDef.annotations.forEach { annotation ->
                            //Log.e("Test", "${annotation.type}")
                            if (annotation.type == "Lcom/decomposer/runtime/PostComposeIr;") {
                                var fileData: Array<String> = emptyArray()
                                var fqName = ""
                                var path = ""
                                var types: Array<String> = emptyArray()
                                var signatures: Array<String> = emptyArray()
                                var strings: Array<String> = emptyArray()
                                var bodies: Array<String> = emptyArray()
                                var declarations: Array<String> = emptyArray()
                                var debugInfo: Array<String> = emptyArray()
                                var backendSpecificMetadata: Array<String> = emptyArray()
                                annotation.elements.forEachIndexed { index, ele ->
                                    when (ele.name) {
                                        "fileData" -> fileData = extractStringArray(ele.value)
                                        "fqName" -> fqName = extractString(ele.value)
                                        "path" -> path = extractString(ele.value)
                                        "types" -> types = extractStringArray(ele.value)
                                        "signatures" -> signatures = extractStringArray(ele.value)
                                        "strings" -> strings = extractStringArray(ele.value)
                                        "bodies" -> bodies = extractStringArray(ele.value)
                                        "declarations" -> declarations = extractStringArray(ele.value)
                                        "debugInfo" -> debugInfo = extractStringArray(ele.value)
                                        "backendSpecificMetadata" -> backendSpecificMetadata = extractStringArray(ele.value)
                                    }
                                    /*
                                    val stringList = mutableListOf<String>()
                                    if (ele.value.valueType == ValueType.ARRAY) {
                                        val arrayValue = ele.value as ArrayEncodedValue  // Cast to a list of elements
                                        arrayValue.value.forEach { item ->
                                            if (item.valueType == ValueType.STRING) {
                                                item as StringEncodedValue
                                                val stringValue = item.value
                                                stringList.add(stringValue)  // Add each string to the list
                                            }
                                        }
                                    }

                                    // Convert the list to an Array<String>
                                    val kotlinArray: Array<String> = stringList.toTypedArray()
                                    kotlinArray.forEach {
                                        Log.e("Test", "Sending $it ")
                                        webSocket.send(it)
                                    }
                                    */
                                }
                                val data = SerializedIrFile(
                                    fileData = fileData,
                                    fqName = fqName,
                                    path = path,
                                    types = types,
                                    signatures = signatures,
                                    strings = strings,
                                    bodies = bodies,
                                    declarations = declarations,
                                    debugInfo = debugInfo,
                                    backendSpecificMetadata = backendSpecificMetadata
                                )
                                debug(data)
                                val json = Json.encodeToString(SerializedIrFile.serializer(), data)
                                //Log.e("Test", json)
                                webSocket.send(json)
                            }
                        }
                    }
                }

                tempDexFile.delete()
            }

            zipFile.close()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error processing APK for class listing.")
        } finally {
            // Clean up temporary directory if needed
            tempDexDir.deleteRecursively()
        }
    }

    fun debug(serializedIrFile: SerializedIrFile) {
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
        val jsonPrinter = JsonFormat.printer()
        val irFileJson = jsonPrinter.print(irFile)
        Log.e("Test", "irFile:\n$irFileJson")

        val irTypes = readMemoryArray(BitEncoding.decodeBytes(types)) {
            IrType.parseFrom(it)
        }
        irTypes.forEach {
            Log.e("Test", "irType:${jsonPrinter.print(it)}")
        }
        val irSignatures = readMemoryArray(BitEncoding.decodeBytes(signatures)) {
            IdSignature.parseFrom(it)
        }
        irSignatures.forEach {
            Log.e("Test", "irSignature:${jsonPrinter.print(it)}")
        }
        val irBodies = readMemoryArray(BitEncoding.decodeBytes(bodies)) {
            var isExpression = true
            var isStatement = true
            try {
                val express = IrExpression.parseFrom(it)
                Log.e("Test", "irExpression:${jsonPrinter.print(express)}")
            } catch (e: Exception) {
                isExpression = false
            }
            if (!isExpression) {
                try {
                    val stats = IrStatement.parseFrom(it)
                    Log.e("Test", "irStatement:${jsonPrinter.print(stats)}")
                } catch (e: Exception) {
                    isStatement = false
                }
            }
            it
        }
        val irDeclarations = readMemoryDeclaration(BitEncoding.decodeBytes(declarations))
        irDeclarations.forEach {
            Log.e("Test", "irDeclaration:${jsonPrinter.print(it)}")
        }
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

    fun extractStringArray(value: EncodedValue): Array<String> {
        val stringList = mutableListOf<String>()
        if (value.valueType == ValueType.ARRAY) {
            val arrayValue = value as ArrayEncodedValue  // Cast to a list of elements
            arrayValue.value.forEach { item ->
                if (item.valueType == ValueType.STRING) {
                    item as StringEncodedValue
                    val stringValue = item.value
                    stringList.add(stringValue)  // Add each string to the list
                }
            }
        }

        // Convert the list to an Array<String>
        return stringList.toTypedArray()
    }

    fun extractString(value: EncodedValue): String {
        if (value.valueType == ValueType.STRING) {
            val stringValue = value as StringEncodedValue
            return stringValue.value
        }
        return ""
    }

    fun start(): WebSocket {
        val request = Request.Builder()
            .url("ws://localhost:9900")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.e("Test", "Connection opened")
                //webSocket.send("Hello, WebSocket!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.e("Test", "Receiving : $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.e("Test", "Receiving bytes : ${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.e("Test", "Closing : $code / $reason")
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("Test", "onFailure ${t.stackTraceToString()}")
            }
        }

        return client.newWebSocket(request, listener)
    }
}


@Composable
fun Empty() {

}

class Sample(
    val name: String,
    val age: Int
)
