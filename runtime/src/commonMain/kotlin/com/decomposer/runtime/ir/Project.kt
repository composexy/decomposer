package com.decomposer.runtime.ir

data class VirtualFileIr(
    val filePath: String,
    val composedIrFile: List<String>?,
    val composedTopLevelIrClasses: Set<List<String>>,
    val originalIrFile: List<String>?,
    val originalTopLevelIrClasses: Set<List<String>>
)

interface ProjectScanner {
    suspend fun fetchProjectSnapshot(): Set<String>
    suspend fun fetchIr(filePath: String): VirtualFileIr
}
