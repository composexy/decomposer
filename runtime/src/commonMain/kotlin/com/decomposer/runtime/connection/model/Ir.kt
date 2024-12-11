package com.decomposer.runtime.connection.model

import kotlinx.serialization.Serializable

@Serializable
class ProjectSnapshot(
    val fileTree: Set<String>,
    val packagesByPath: Map<String, String>
)

@Serializable
class VirtualFileIr(
    val filePath: String,
    val composedIrFile: List<String>,
    val composedTopLevelIrClasses: Set<List<String>>,
    val composedStandardDump: List<String>,
    val originalIrFile: List<String>,
    val originalTopLevelIrClasses: Set<List<String>>,
    val originalStandardDump: List<String> = emptyList()
)
