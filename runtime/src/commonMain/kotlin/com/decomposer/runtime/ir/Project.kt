package com.decomposer.runtime.ir

internal data class VirtualFileIr(
    val filePath: String,
    val composedIrFile: List<String>?,
    val composedTopLevelIrClasses: Set<List<String>>,
    val originalIrFile: List<String>?,
    val originalTopLevelIrClasses: Set<List<String>>
)

internal interface ProjectScanner {
    suspend fun fetchProjectSnapshot(): Set<String>
    suspend fun fetchIr(filePath: String): VirtualFileIr
}
