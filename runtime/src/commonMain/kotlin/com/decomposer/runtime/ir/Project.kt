package com.decomposer.runtime.ir

import kotlinx.serialization.Serializable

@Serializable
data class IrFetchRequest(
    val element: Element
) {
    @Serializable
    data class Element(
        val filePath: String
    )
}

@Serializable
data class IrFetchResult(
    val element: Element
) {
    @Serializable
    data class Element(
        val filePath: String,
        val composedIrFile: List<String>?,
        val composedTopLevelIrClasses: Set<List<String>>,
        val originalIrFile: List<String>?,
        val originalTopLevelIrClasses: Set<List<String>>,
    )
}

@Serializable
data class ProjectStructure(
    val projectFilePaths: Set<String>
)

interface ProjectScanner {
    suspend fun fetchProjectStructure(): ProjectStructure
    suspend fun fetchIr(request: IrFetchRequest): IrFetchResult
}
