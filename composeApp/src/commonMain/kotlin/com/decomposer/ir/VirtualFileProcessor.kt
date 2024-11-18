package com.decomposer.ir

import com.decomposer.runtime.connection.model.VirtualFileIr
import com.decomposer.runtime.ir.ClassOrFile
import com.squareup.moshi.Moshi
import com.squareup.wire.WireJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.metadata.jvm.deserialization.BitEncoding

internal class VirtualFileProcessor {
    private val processorScope = CoroutineScope(Dispatchers.Default)
    private val jsonAdapter = kotlin.run {
        val moshi = Moshi.Builder()
            .add(WireJsonAdapterFactory())
            .build()
        moshi.adapter(ClassOrFile::class.java)
    }

    fun processVirtualFileIr(ir: VirtualFileIr) = processorScope.launch {
        if (ir.originalIrFile.isNotEmpty()) {
            processOriginalIrFile(ir.filePath, ir.originalIrFile)
        }
        ir.originalTopLevelIrClasses.forEach {
            processOriginalIrClass(ir.filePath, it)
        }
        if (ir.composedIrFile.isNotEmpty()) {
            processComposedIrFile(ir.filePath, ir.composedIrFile)
        }
        ir.composedTopLevelIrClasses.forEach {
            processComposedIrClass(ir.filePath, it)
        }
    }

    private fun processComposedIrFile(path: String, data: List<String>) {
        val protoByteArray = BitEncoding.decodeBytes(data.toTypedArray())
        val file = ClassOrFile.ADAPTER.decode(protoByteArray)
        file.printJson()
    }

    private fun processComposedIrClass(path: String, data: List<String>) {
        val protoByteArray = BitEncoding.decodeBytes(data.toTypedArray())
        val file = ClassOrFile.ADAPTER.decode(protoByteArray)
        file.printJson()
    }

    private fun processOriginalIrFile(path: String, data: List<String>) {
        val protoByteArray = BitEncoding.decodeBytes(data.toTypedArray())
        val file = ClassOrFile.ADAPTER.decode(protoByteArray)
        file.printJson()
    }

    private fun processOriginalIrClass(path: String, data: List<String>) {
        val protoByteArray = BitEncoding.decodeBytes(data.toTypedArray())
        val file = ClassOrFile.ADAPTER.decode(protoByteArray)
        file.printJson()
    }

    private fun ClassOrFile.printJson() {
        val json = jsonAdapter.indent("  ").toJson(this)
        println(json)
    }
}
