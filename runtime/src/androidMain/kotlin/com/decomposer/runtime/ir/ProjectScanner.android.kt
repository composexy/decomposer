package com.decomposer.runtime.ir

import android.content.Context
import android.os.Build
import android.util.Log
import com.decomposer.runtime.Logger
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

internal class AndroidProjectScanner(
    private val context: Context
) : ProjectScanner, Logger by AndroidLogger {
    private val uncommitedFilePaths = mutableSetOf<String>()
    private val uncommitedPackageNamesByPath = mutableMapOf<String, String>()
    private val uncommitedComposedIrFiles = mutableMapOf<String, List<String>>()
    private val uncommitedOriginalIrFiles = mutableMapOf<String, List<String>>()
    private val uncommitedComposedIrTopLevelClasses =
        mutableMapOf<String, MutableSet<List<String>>>()
    private val uncommitedOriginalIrTopLevelClasses =
        mutableMapOf<String, MutableSet<List<String>>>()
    private val uncommitedComposedIrDump = mutableMapOf<String, List<String>>()
    private val uncommitedOriginalIrDump = mutableMapOf<String, List<String>>()

    private var projectStructure: Pair<Set<String>, Map<String, String>>? = null
    private var projectFiles: Map<String, VirtualFileIr>? = null

    private val projectStructureWaiters =
        mutableListOf<Continuation<Pair<Set<String>, Map<String, String>>>>()
    private val irWaitersByPath = mutableMapOf<String, Continuation<VirtualFileIr>>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun fetchProjectSnapshot(): Pair<Set<String>, Map<String, String>> {
        return suspendCoroutine { continuation ->
            val structure = projectStructure
            if (structure != null) {
                continuation.resumeWith(Result.success(structure))
            } else {
                projectStructureWaiters.add(continuation)
            }
        }
    }

    override suspend fun fetchIr(
        filePath: String
    ): VirtualFileIr = suspendCoroutine { continuation ->
        val projectFiles = projectFiles
        if (projectFiles != null) {
            val virtualFileIr = projectFiles[filePath] ?: VirtualFileIr(filePath)
            continuation.resumeWith(Result.success(virtualFileIr))
        } else {
            irWaitersByPath[filePath] = continuation
        }
    }

    private fun clearUncommitedData() {
        uncommitedFilePaths.clear()
        uncommitedPackageNamesByPath.clear()
        uncommitedComposedIrFiles.clear()
        uncommitedComposedIrTopLevelClasses.clear()
        uncommitedComposedIrDump.clear()
        uncommitedOriginalIrFiles.clear()
        uncommitedOriginalIrTopLevelClasses.clear()
        uncommitedOriginalIrDump.clear()
    }

    internal fun scanProject() {
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
                        log(Logger.Level.WARNING, TAG, "Cannot delete temporary file ${virtualDexFile.name}")
                    }
                }

                commitProjectData()
                resumeProjectStructureWaiters()
                resumeIrWaiters()
            } catch (ex: Exception) {
                log(Logger.Level.WARNING, TAG, ex.stackTraceToString())
            } finally {
                zipFile.close()
                scanDir.deleteRecursively()
                clearUncommitedData()
            }
        }
    }

    private fun commitProjectData() {
        projectStructure = Pair<Set<String>, Map<String, String>>(
            first = mutableSetOf<String>().also {
                it.addAll(uncommitedFilePaths)
            },
            second = mutableMapOf<String, String>().also {
                it.putAll(uncommitedPackageNamesByPath)
            }
        )
        projectFiles = mutableMapOf<String, VirtualFileIr>().also {
            it.putAll(uncommitedFilePaths.associateWith { filePath ->
                VirtualFileIr(
                    filePath = filePath,
                    composedIrFile = uncommitedComposedIrFiles[filePath],
                    composedTopLevelIrClasses = uncommitedComposedIrTopLevelClasses[filePath] ?: emptySet(),
                    composedStandardDump = uncommitedComposedIrDump[filePath] ?: emptyList(),
                    originalIrFile = uncommitedOriginalIrFiles[filePath],
                    originalTopLevelIrClasses = uncommitedOriginalIrTopLevelClasses[filePath] ?: emptySet(),
                    originalStandardDump = uncommitedComposedIrDump[filePath] ?: emptyList(),
                )
            })
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
            var packageName: String? = null
            var dump: List<String> = emptyList()
            var ir: List<String>? = null
            var isFileFacade: Boolean? = null
            annotation.elements.forEach {
                when (it.name) {
                    "filePath" -> filePath = extractString(it.value)
                    "packageName" -> packageName = extractString(it.value)
                    "isFileFacade" -> isFileFacade = extractIsFileFacade(it.value)
                    "standardDump" -> dump = extractStringArray(it.value) ?: emptyList()
                    "data" -> ir = extractStringArray(it.value)
                    else -> log(Logger.Level.WARNING, TAG, "Unexpected field ${it.name} on ${annotation.type}")
                }
            }
            if (ir != null && filePath != null && isFileFacade != null) {
                processAnnotation(ir!!, dump, filePath!!, packageName!!, isFileFacade!!, composed)
            }
        }
    }

    private fun processAnnotation(
        ir: List<String>,
        dump: List<String>,
        filePath: String,
        packageName: String,
        fileFacade: Boolean,
        composed: Boolean
    ) {
        uncommitedFilePaths.add(filePath)
        val existingPackage = uncommitedPackageNamesByPath[filePath]
        if (existingPackage != null) {
            if (packageName != existingPackage) {
                log(Logger.Level.ERROR, TAG, "Package name different: $packageName, $existingPackage")
            }
        } else {
            uncommitedPackageNamesByPath[filePath] = packageName
        }
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
        if (dump.isNotEmpty()) {
            if (composed && !uncommitedComposedIrDump.containsKey(filePath)) {
                uncommitedComposedIrDump[filePath] = dump
            } else if (!composed && !uncommitedOriginalIrDump.containsKey(filePath)) {
                uncommitedOriginalIrDump[filePath] = dump
            }
        }
    }

    private fun extractIsFileFacade(value: EncodedValue): Boolean? {
        return if (value.valueType == ValueType.BOOLEAN) {
            value as BooleanEncodedValue
            value.value
        } else null
    }

    private fun extractString(value: EncodedValue): String? {
        return if (value.valueType == ValueType.STRING) {
            value as StringEncodedValue
            value.value
        } else null
    }

    private fun extractStringArray(value: EncodedValue): List<String>? {
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
            log(Logger.Level.WARNING, TAG, "Cannot find project structure!")
        }
        projectStructureWaiters.clear()
    }

    private fun resumeIrWaiters() {
        irWaitersByPath.forEach {
            val filePath = it.key
            val waiter = it.value
            val virtualFileIr = projectFiles?.get(filePath) ?: VirtualFileIr(filePath = filePath)
            waiter.resumeWith(Result.success(virtualFileIr))
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
