package com.decomposer.runtime.ir

internal data class VirtualFileIr(
    val filePath: String,
    val composedIrFile: List<String>? = null,
    val composedTopLevelIrClasses: Set<List<String>> = emptySet(),
    val composedStandardDump: List<String> = emptyList(),
    val originalIrFile: List<String>? = null,
    val originalTopLevelIrClasses: Set<List<String>> = emptySet(),
    val originalStandardDump: List<String> = emptyList()
)

internal interface ProjectScanner {
    suspend fun fetchProjectSnapshot(): Pair<Set<String>, Map<String, String>>
    suspend fun fetchIr(filePath: String): VirtualFileIr
}
