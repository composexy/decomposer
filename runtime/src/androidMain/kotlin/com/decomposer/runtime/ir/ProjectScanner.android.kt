package com.decomposer.runtime.ir

import android.content.Context
import android.os.Build
import com.decomposer.runtime.AndroidLogger
import com.decomposer.runtime.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.ValueType
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.DexFile
import org.jf.dexlib2.iface.value.ArrayEncodedValue
import org.jf.dexlib2.iface.value.BooleanEncodedValue
import org.jf.dexlib2.iface.value.EncodedValue
import org.jf.dexlib2.iface.value.StringEncodedValue
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

internal class AndroidProjectScanner(
    private val context: Context,
    private val preloadAllIr: Boolean,
    private var cacheIr: Boolean,
    packagePrefixes: List<String>?
) : ProjectScanner, Logger by AndroidLogger {
    private var packageDescriptors = packagePrefixes?.map {
        packageToDescriptor(it)
    }
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
    private val uncommitedIrLocations = mutableMapOf<String, IrLocation>()

    private var scannedProjectStructure: Pair<Set<String>, Map<String, String>>? = null
    private var scannedProjectFiles: MutableMap<String, VirtualFileIr?>? = null
    private var scannedIrLocations: Map<String, IrLocation>? = null

    private val projectStructureWaiters =
        mutableListOf<Continuation<Pair<Set<String>, Map<String, String>>>>()
    private val irWaitersByPath = mutableMapOf<String, Continuation<VirtualFileIr>>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun fetchProjectSnapshot(): Pair<Set<String>, Map<String, String>> {
        return suspendCoroutine { continuation ->
            val structure = scannedProjectStructure
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
        val projectFiles = scannedProjectFiles
        val irLocations = scannedIrLocations
        if (projectFiles != null) {
            if (projectFiles.containsKey(filePath)) {
                val virtualFileIr = projectFiles[filePath]
                when {
                    virtualFileIr != null -> {
                        continuation.resumeWith(Result.success(virtualFileIr))
                        if (!cacheIr && !preloadAllIr) {
                            projectFiles[filePath] = null
                        }
                    }
                    irLocations != null && irLocations.containsKey(filePath) -> {
                        val irLocation = irLocations[filePath]!!
                        irWaitersByPath[filePath] = continuation
                        loadIr(filePath, irLocation)
                    }
                    else -> continuation.resumeWith(Result.success(VirtualFileIr(filePath)))
                }
            } else {
                continuation.resumeWith(Result.success(VirtualFileIr(filePath)))
            }
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
        uncommitedIrLocations.clear()
    }

    private fun loadDexFile(dexEntry: ZipEntry, scanDir: File, zipFile: ZipFile): DexFile {
        val virtualDexFile = File(scanDir, dexEntry.name)
        zipFile.getInputStream(dexEntry).use { input ->
            virtualDexFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val opCode = Opcodes.forApi(Build.VERSION.SDK_INT)
        return DexFileFactory.loadDexFile(virtualDexFile, opCode).also {
            if (!virtualDexFile.delete()) {
                log(
                    Logger.Level.WARNING,
                    TAG,
                    "Cannot delete temporary file ${virtualDexFile.name}"
                )
            }
        }
    }

    private fun loadIr(filePath: String, irLocation: IrLocation) {
        coroutineScope.launch {
            val apkFile = File(context.applicationInfo.sourceDir)
            val scanDir = context.getDir(SCANNER_DIR, Context.MODE_PRIVATE)
            val zipFile = ZipFile(apkFile)
            try {
                val dexEntry = zipFile.entries().asSequence()
                    .filter { it.name == irLocation.dexFileName }
                    .single()
                val dexFile = withContext(Dispatchers.IO) {
                    loadDexFile(dexEntry, scanDir, zipFile)
                }
                val irDataPart = IrDataPart()
                dexFile.classes.filter { it.type in irLocation.classDescriptors }.forEach {
                    loadDexClass(
                        dexFileName = dexEntry.name,
                        clazz = it,
                        irDataPart = irDataPart,
                        loadProjectStructure = false,
                        loadIr = true,
                        loadIrLocation = false
                    )
                }
                scannedProjectFiles!![filePath] = VirtualFileIr(
                    filePath = filePath,
                    composedIrFile = irDataPart.composedIrFiles[filePath],
                    composedTopLevelIrClasses = irDataPart.composedIrTopLevelClasses[filePath]
                        ?: emptySet(),
                    composedStandardDump = irDataPart.composedIrDump[filePath] ?: emptyList(),
                    originalIrFile = irDataPart.originalIrFiles[filePath],
                    originalTopLevelIrClasses = irDataPart.originalIrTopLevelClasses[filePath]
                        ?: emptySet(),
                    originalStandardDump = irDataPart.originalIrDump[filePath] ?: emptyList(),
                )
                resumeIrWaiters()
            } catch (ex: Exception) {
                log(Logger.Level.WARNING, TAG, ex.stackTraceToString())
            } finally {
                zipFile.close()
                scanDir.deleteRecursively()
            }
        }
    }

    private fun packageToDescriptor(packageName: String): String {
        return "L${packageName.replace('.', '/')}"
    }

    internal fun scanProject() {
        coroutineScope.launch {
            val apkFile = File(context.applicationInfo.sourceDir)
            val scanDir = context.getDir(SCANNER_DIR, Context.MODE_PRIVATE)
            val zipFile = ZipFile(apkFile)
            val loadJobs = mutableListOf<Deferred<IrDataPart>>()
            clearUncommitedData()

            try {
                val dexEntries = zipFile.entries().asSequence()
                    .filter { it.name.endsWith(".dex") }
                    .toList()

                for (dexEntry in dexEntries) {
                    val dexFile = withContext(Dispatchers.IO) {
                        loadDexFile(dexEntry, scanDir, zipFile)
                    }
                    val job = async(Dispatchers.Default) {
                        val irDataPart = IrDataPart()
                        dexFile.classes.forEach { clazz ->
                            val packageDescriptors = packageDescriptors
                            if (packageDescriptors == null
                                || packageDescriptors.any { clazz.type.startsWith(it) }
                            ) {
                                loadDexClass(
                                    dexFileName = dexEntry.name,
                                    clazz = clazz,
                                    irDataPart = irDataPart,
                                    loadProjectStructure = true,
                                    loadIr = preloadAllIr,
                                    loadIrLocation = true
                                )
                            }
                        }
                        irDataPart
                    }
                    loadJobs.add(job)
                }

                loadJobs.forEach { job ->
                    val irParts = job.await()
                    synchronized(uncommitedFilePaths) {
                        uncommitedFilePaths.addAll(irParts.filePaths)
                    }
                    synchronized(uncommitedPackageNamesByPath) {
                        uncommitedPackageNamesByPath.putAll(irParts.packageNamesByPath)
                    }
                    synchronized(uncommitedComposedIrFiles) {
                        uncommitedComposedIrFiles.putAll(irParts.composedIrFiles)
                    }
                    synchronized(uncommitedOriginalIrFiles) {
                        uncommitedOriginalIrFiles.putAll(irParts.originalIrFiles)
                    }
                    synchronized(uncommitedComposedIrTopLevelClasses) {
                        irParts.composedIrTopLevelClasses.forEach { entry ->
                            val filePath = entry.key
                            val topLevelClasses = entry.value
                            if (!uncommitedComposedIrTopLevelClasses.containsKey(filePath)) {
                                uncommitedComposedIrTopLevelClasses[filePath] = topLevelClasses
                            } else {
                                uncommitedComposedIrTopLevelClasses[filePath]!! += topLevelClasses
                            }
                        }
                    }
                    synchronized(uncommitedOriginalIrTopLevelClasses) {
                        irParts.originalIrTopLevelClasses.forEach { entry ->
                            val filePath = entry.key
                            val topLevelClasses = entry.value
                            if (!uncommitedOriginalIrTopLevelClasses.containsKey(filePath)) {
                                uncommitedOriginalIrTopLevelClasses[filePath] = topLevelClasses
                            } else {
                                uncommitedOriginalIrTopLevelClasses[filePath]!! += topLevelClasses
                            }
                        }
                    }
                    synchronized(uncommitedComposedIrDump) {
                        uncommitedComposedIrDump.putAll(irParts.composedIrDump)
                    }
                    synchronized(uncommitedOriginalIrDump) {
                        uncommitedOriginalIrDump.putAll(irParts.originalIrDump)
                    }
                    synchronized(uncommitedIrLocations) {
                        uncommitedIrLocations.putAll(irParts.irLocationByFilePath)
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
        scannedProjectStructure = Pair<Set<String>, Map<String, String>>(
            first = mutableSetOf<String>().also {
                it.addAll(uncommitedFilePaths)
            },
            second = mutableMapOf<String, String>().also {
                it.putAll(uncommitedPackageNamesByPath)
            }
        )
        scannedIrLocations = mutableMapOf<String, IrLocation>().also {
            it.putAll(uncommitedIrLocations)
        }
        scannedProjectFiles = mutableMapOf<String, VirtualFileIr?>().also {
            it.putAll(uncommitedFilePaths.associateWith { filePath ->
                if (preloadAllIr) {
                    VirtualFileIr(
                        filePath = filePath,
                        composedIrFile = uncommitedComposedIrFiles[filePath],
                        composedTopLevelIrClasses = uncommitedComposedIrTopLevelClasses[filePath]
                            ?: emptySet(),
                        composedStandardDump = uncommitedComposedIrDump[filePath] ?: emptyList(),
                        originalIrFile = uncommitedOriginalIrFiles[filePath],
                        originalTopLevelIrClasses = uncommitedOriginalIrTopLevelClasses[filePath]
                            ?: emptySet(),
                        originalStandardDump = uncommitedComposedIrDump[filePath] ?: emptyList(),
                    )
                } else null
            })
        }
    }

    private fun loadDexClass(
        dexFileName: String,
        clazz: ClassDef,
        irDataPart: IrDataPart,
        loadProjectStructure: Boolean,
        loadIr: Boolean,
        loadIrLocation: Boolean
    ) {
        fun loadAnnotation(
            ir: List<String>,
            dump: List<String>,
            filePath: String,
            packageName: String,
            fileFacade: Boolean,
            composed: Boolean,
            irDataPart: IrDataPart
        ) {
            fun loadProjectStructure() {
                irDataPart.filePaths.add(filePath)
                val existingPackage = irDataPart.packageNamesByPath[filePath]
                if (existingPackage != null) {
                    if (packageName != existingPackage) {
                        log(
                            Logger.Level.ERROR, TAG,
                            "Package different: $packageName, $existingPackage"
                        )
                    }
                } else {
                    irDataPart.packageNamesByPath[filePath] = packageName
                }
            }

            fun loadIr() {
                when {
                    fileFacade && composed -> irDataPart.composedIrFiles[filePath] = ir
                    fileFacade && !composed -> irDataPart.originalIrFiles[filePath] = ir
                    !fileFacade && composed -> {
                        if (!irDataPart.composedIrTopLevelClasses.containsKey(filePath)) {
                            irDataPart.composedIrTopLevelClasses[filePath] = mutableSetOf()
                        }
                        irDataPart.composedIrTopLevelClasses[filePath]!!.add(ir)
                    }
                    else -> {
                        if (!irDataPart.originalIrTopLevelClasses.containsKey(filePath)) {
                            irDataPart.originalIrTopLevelClasses[filePath] = mutableSetOf()
                        }
                        irDataPart.originalIrTopLevelClasses[filePath]!!.add(ir)
                    }
                }
                if (dump.isNotEmpty()) {
                    if (composed && !irDataPart.composedIrDump.containsKey(filePath)) {
                        irDataPart.composedIrDump[filePath] = dump
                    } else if (!composed && !uncommitedOriginalIrDump.containsKey(filePath)) {
                        irDataPart.originalIrDump[filePath] = dump
                    }
                }
            }

            fun loadIrLocation() {
                if (!irDataPart.irLocationByFilePath.containsKey(filePath)) {
                    irDataPart.irLocationByFilePath[filePath] = IrLocation(dexFileName)
                }
                irDataPart.irLocationByFilePath[filePath]!!.classDescriptors.add(clazz.type)
            }

            if (loadProjectStructure) loadProjectStructure()
            if (loadIr) loadIr()
            if (loadIrLocation) loadIrLocation()
        }

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
                loadAnnotation(ir!!, dump, filePath!!, packageName!!, isFileFacade!!, composed, irDataPart)
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
        val structure = scannedProjectStructure
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
            val virtualFileIr = scannedProjectFiles?.get(filePath) ?: VirtualFileIr(filePath = filePath)
            waiter.resumeWith(Result.success(virtualFileIr))
        }
        irWaitersByPath.clear()
        if (!cacheIr && !preloadAllIr) {
            val projectFiles = scannedProjectFiles
            projectFiles?.keys?.forEach {
                projectFiles[it] = null
            }
        }
    }

    companion object {
        private const val SCANNER_DIR = "com.decomposer.runtime.ir.projectScanner"
        private const val TAG = "AndroidProjectScanner"
        private const val PRE_COMPOSE_IR_SIGNATURE = "Lcom/decomposer/runtime/PreComposeIr;"
        private const val POST_COMPOSE_IR_SIGNATURE = "Lcom/decomposer/runtime/PostComposeIr;"
    }
}

internal class IrDataPart(
    val filePaths: MutableSet<String> = mutableSetOf(),
    val packageNamesByPath: MutableMap<String, String> = mutableMapOf(),
    val composedIrFiles: MutableMap<String, List<String>> = mutableMapOf(),
    val originalIrFiles: MutableMap<String, List<String>> = mutableMapOf(),
    val composedIrTopLevelClasses: MutableMap<String, MutableSet<List<String>>> =
        mutableMapOf(),
    val originalIrTopLevelClasses: MutableMap<String, MutableSet<List<String>>> =
        mutableMapOf(),
    val composedIrDump: MutableMap<String, List<String>> = mutableMapOf(),
    val originalIrDump: MutableMap<String, List<String>> = mutableMapOf(),
    val irLocationByFilePath: MutableMap<String, IrLocation> = mutableMapOf()
)

internal class IrLocation(
    val dexFileName: String,
    val classDescriptors: MutableSet<String> = mutableSetOf()
)
