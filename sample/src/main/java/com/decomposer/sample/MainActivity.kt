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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.ValueType
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.value.ArrayEncodedValue
import org.jf.dexlib2.iface.value.StringEncodedValue
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
                                annotation.elements.forEach { ele ->
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
                                }
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

    fun start(): WebSocket {
        val request = Request.Builder()
            .url("ws://localhost:9900")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.e("Test", "Connection opened")
                webSocket.send("Hello, WebSocket!")
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
