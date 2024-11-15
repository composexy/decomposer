package com.decomposer.ir

import com.decomposer.runtime.connection.model.VirtualFileIr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VirtualFileProcessor {
    private val processorScope = CoroutineScope(Dispatchers.Default)

    fun processVirtualFileIr(ir: VirtualFileIr) = processorScope.launch {

    }
}
