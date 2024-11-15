package com.decomposer.runtime.ir

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.ValueType
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.value.ArrayEncodedValue
import org.jf.dexlib2.iface.value.BooleanEncodedValue
import org.jf.dexlib2.iface.value.EncodedValue
import org.jf.dexlib2.iface.value.StringEncodedValue
import java.io.File
import java.util.zip.ZipFile
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

class AndroidProjectScanner(context: Context, autoScan: Boolean): ProjectScanner {

    private val uncommitedFilePaths = mutableSetOf<String>()
    private val uncommitedComposedIrFiles = mutableMapOf<String, List<String>>()
    private val uncommitedOriginalIrFiles = mutableMapOf<String, List<String>>()
    private val uncommitedComposedIrTopLevelClasses = mutableMapOf<String, MutableSet<List<String>>>()
    private val uncommitedOriginalIrTopLevelClasses = mutableMapOf<String, MutableSet<List<String>>>()

    private var projectStructure: Set<String>? = null
    private var projectFiles: Map<String, ProjectFile>? = null

    private val projectStructureWaiters = mutableListOf<Continuation<Set<String>>>()
    private val irWaitersByPath = mutableMapOf<String, Continuation<VirtualFileIr>>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        if (autoScan) scanProject(context)
    }

    override suspend fun fetchProjectStructure(): Set<String> = suspendCoroutine { continuation ->
        val structure = projectStructure
        if (structure != null) {
            continuation.resumeWith(Result.success(structure))
        } else {
            projectStructureWaiters.add(continuation)
        }
    }

    override suspend fun fetchIr(filePath: String): VirtualFileIr = suspendCoroutine { continuation ->
        val projectFiles = projectFiles
        if (projectFiles != null) {
            val projectFile = projectFiles[filePath]
            val virtualFileIr = VirtualFileIr(
                filePath = filePath,
                composedIrFile = projectFile?.composedIrFile,
                composedTopLevelIrClasses = projectFile?.composedTopLevelIrClasses ?: emptySet(),
                originalIrFile = projectFile?.originalIrFile,
                originalTopLevelIrClasses = projectFile?.originalTopLevelIrClasses ?: emptySet()
            )
            continuation.resumeWith(
                Result.success(virtualFileIr)
            )
        } else {
            irWaitersByPath[filePath] = continuation
        }
    }

    private fun clearUncommitedData() {
        uncommitedFilePaths.clear()
        uncommitedComposedIrFiles.clear()
        uncommitedComposedIrTopLevelClasses.clear()
        uncommitedOriginalIrFiles.clear()
        uncommitedOriginalIrTopLevelClasses.clear()
    }

    private fun scanProject(context: Context) {
        coroutineScope.launch {
            val apkFile = File(context.applicationInfo.sourceDir)
            val scanDir = context.getDir(SCANNER_DIR, Context.MODE_PRIVATE)
            val zipFile = ZipFile(apkFile)

            clearUncommitedData()

            try {
                val dexEntries = zipFile.entries().asSequence()
                    .filter { it.name.endsWith(".dex") }
                    .toList()

                for (dexEntry in dexEntries) {
                    val virtualDexFile = File(scanDir, dexEntry.name)
                    zipFile.getInputStream(dexEntry).use { input ->
                        virtualDexFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    val opCode = Opcodes.forApi(Build.VERSION.SDK_INT)
                    val dexFile = DexFileFactory.loadDexFile(virtualDexFile, opCode)
                    dexFile.classes.forEach { processDexClass(it) }
                    if (!virtualDexFile.delete()) {
                        Log.w(TAG, "Cannot delete temporary file ${virtualDexFile.name}")
                    }
                }

                commitProjectData()
                resumeProjectStructureWaiters()
                resumeIrWaiters()
            } catch (ex: Exception) {
                Log.e(TAG, ex.stackTraceToString())
            } finally {
                zipFile.close()
                scanDir.deleteRecursively()
                clearUncommitedData()
            }
        }
    }

    private fun commitProjectData() {
        projectStructure = uncommitedFilePaths
        projectFiles = uncommitedFilePaths.associateWith { filePath ->
            ProjectFile(
                projectFilePath = filePath,
                composedIrFile = uncommitedComposedIrFiles[filePath],
                composedTopLevelIrClasses = uncommitedComposedIrTopLevelClasses[filePath] ?: emptySet(),
                originalIrFile = uncommitedOriginalIrFiles[filePath],
                originalTopLevelIrClasses = uncommitedOriginalIrTopLevelClasses[filePath] ?: emptySet()
            )
        }
    }

    private fun processDexClass(clazz: ClassDef) {
        clazz.annotations.forEach { annotation ->
            val type = annotation.type
            if (type != PRE_COMPOSE_IR_SIGNATURE && type != POST_COMPOSE_IR_SIGNATURE) {
                return@forEach
            }
            val composed = type == POST_COMPOSE_IR_SIGNATURE
            var filePath: String? = null
            var ir: List<String>? = null
            var isFileFacade: Boolean? = null
            annotation.elements.forEach {
                when (it.name) {
                    "filePath" -> filePath = extractFilePath(it.value)
                    "isFileFacade" -> isFileFacade = extractIsFileFacade(it.value)
                    "data" -> ir = extractIr(it.value)
                    else -> Log.w(TAG, "Unexpected field ${it.name} on ${annotation.type}")
                }
            }
            if (ir != null && filePath != null && isFileFacade != null) {
                processAnnotation(ir!!, filePath!!, isFileFacade!!, composed)
            }
        }
    }

    private fun processAnnotation(
        ir: List<String>,
        filePath: String,
        fileFacade: Boolean,
        composed: Boolean
    ) {
        uncommitedFilePaths.add(filePath)
        when {
            fileFacade && composed -> uncommitedComposedIrFiles[filePath] = ir
            fileFacade && !composed -> uncommitedOriginalIrFiles[filePath] = ir
            !fileFacade && composed -> {
                uncommitedComposedIrTopLevelClasses.putIfAbsent(filePath, mutableSetOf())
                uncommitedComposedIrTopLevelClasses[filePath]!!.add(ir)
            }
            else -> {
                uncommitedOriginalIrTopLevelClasses.putIfAbsent(filePath, mutableSetOf())
                uncommitedOriginalIrTopLevelClasses[filePath]!!.add(ir)
            }
        }
    }

    private fun extractIsFileFacade(value: EncodedValue): Boolean? {
        return if (value.valueType == ValueType.BOOLEAN) {
            value as BooleanEncodedValue
            value.value
        } else null
    }

    private fun extractFilePath(value: EncodedValue): String? {
        return if (value.valueType == ValueType.STRING) {
            value as StringEncodedValue
            value.value
        } else null
    }

    private fun extractIr(value: EncodedValue): List<String>? {
        return if (value.valueType == ValueType.ARRAY) {
            val stringList = mutableListOf<String>()
            val arrayValue = value as ArrayEncodedValue
            arrayValue.value.forEach { item ->
                if (item.valueType == ValueType.STRING) {
                    item as StringEncodedValue
                    val stringValue = item.value
                    stringList.add(stringValue)
                }
            }
            stringList
        } else null
    }

    private fun resumeProjectStructureWaiters() {
        val structure = projectStructure
        if (structure != null) {
            projectStructureWaiters.forEach {
                it.resumeWith(Result.success(structure))
            }
        } else {
            Log.w(TAG, "Cannot find project structure!")
        }
        projectStructureWaiters.clear()
    }

    private fun resumeIrWaiters() {
        irWaitersByPath.forEach {
            val filePath = it.key
            val waiter = it.value
            val projectFile = projectFiles?.get(filePath)
            val virtualFileIr = if (projectFile == null) {
                VirtualFileIr(
                    filePath = filePath,
                    composedIrFile = null,
                    composedTopLevelIrClasses = emptySet(),
                    originalIrFile = null,
                    originalTopLevelIrClasses = emptySet()
                )
            } else {
                VirtualFileIr(
                    filePath = filePath,
                    composedIrFile = projectFile.composedIrFile,
                    composedTopLevelIrClasses = projectFile.composedTopLevelIrClasses,
                    originalIrFile = projectFile.originalIrFile,
                    originalTopLevelIrClasses = projectFile.originalTopLevelIrClasses
                )
            }
            waiter.resumeWith(
                Result.success(virtualFileIr)
            )
        }
        irWaitersByPath.clear()
    }

    companion object {
        private const val SCANNER_DIR = "com.decomposer.runtime.ir.projectScanner"
        private const val TAG = "AndroidProjectScanner"
        private const val PRE_COMPOSE_IR_SIGNATURE = "Lcom/decomposer/runtime/PreComposeIr;"
        private const val POST_COMPOSE_IR_SIGNATURE = "Lcom/decomposer/runtime/PostComposeIr;"
    }
}

internal data class ProjectFile(
    val projectFilePath: String,
    val composedIrFile: List<String>? = null,
    val composedTopLevelIrClasses: Set<List<String>> = emptySet(),
    val originalIrFile: List<String>? = null,
    val originalTopLevelIrClasses: Set<List<String>> = emptySet(),
)
