package com.decomposer.runtime.ir

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionGroup
import androidx.compose.ui.R
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import com.decomposer.runtime.Logger
import com.decomposer.runtime.compose.CompositionExtractor
import com.decomposer.runtime.connection.model.CompositionRoots
import com.decomposer.runtime.connection.model.Root
import java.lang.reflect.Field

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
@Suppress("UNCHECKED_CAST")
internal class AndroidCompositionExtractor(
    private val context: Context
) : CompositionExtractor(AndroidLogger), Logger by AndroidLogger  {

    private val uiDispatcher = AndroidUiDispatcher.Main
    private val frameClock = uiDispatcher[MonotonicFrameClock]
    private var windowManager: Any
    private var viewsField: Field
    private val compositionRoots = mutableListOf<CompositionData>()

    init {
        val windowManagerClazz = Class.forName(WINDOW_MANAGER_GLOBAL)
        val getInstanceMethod = windowManagerClazz.getMethod(WINDOW_MANAGER_GET_INSTANCE)
        windowManager = getInstanceMethod.invoke(null)!!
        viewsField = windowManagerClazz.getDeclaredField(WINDOW_MANAGER_VIEWS).also {
            it.isAccessible = true
        }
        enableDebugInspector()
    }

    override suspend fun extractCompositionRoots(): CompositionRoots {
        val clock = frameClock ?: throw IllegalArgumentException("Cannot find frame clock!")
        return clock.withFrameNanos {
            val rootViews = getAllRootViews()
            CompositionRoots(extractCompositionData(rootViews))
        }
    }

    private fun extractCompositionData(rootViews: List<View>): List<Root> {
        compositionRoots.clear()
        rootViews.forEach {
            compositionRoots.addAll(it.compositionRoots)
        }
        return compositionRoots.map {
            dumpCompositionData(it)
            map(it)
        }
    }

    private fun getAllRootViews(): List<View> {
        return if (Build.VERSION.SDK_INT >= 29) {
            WindowInspector.getGlobalWindowViews()
        } else {
            viewsField.get(windowManager) as List<View>
        }
    }

    private fun enableDebugInspector() {
        isDebugInspectorInfoEnabled = true
    }

    override fun dumpCompositionData(data: CompositionData) {
        if (!DEBUG) return
        data.compositionGroups.forEachIndexed { index, group ->
            dumpGroup(index, group)
        }
    }

    private fun dumpGroup(index: Int, group: CompositionGroup) {
        val groupInfo = buildString {
            append("Group $index: ${group.key}, ")
            append("node ${group.node}, ")
            append("sourceInfo ${group.sourceInfo}, ")
            append("identity ${group.identity}, ")
            append("slotSize: ${group.slotsSize}, ")
            append("groupSize: ${group.groupSize}\n")
        }
        log(Logger.Level.DEBUG, TAG, groupInfo)
        group.data.forEachIndexed { i, d ->
            log(Logger.Level.DEBUG, TAG, "Data $i: $d\n")
        }
        group.compositionGroups.forEachIndexed { i, g ->
            dumpGroup(i, g)
        }
    }

    private val View.compositionRoots: Set<CompositionData>
        get() {
            val children = mutableListOf(this)
            while (children.isNotEmpty()) {
                val next = children.last()
                if (next.getTag(R.id.inspection_slot_table_set) != null) {
                    return next.getTag(R.id.inspection_slot_table_set) as Set<CompositionData>
                }
                children.remove(next)
                if (next is ViewGroup) {
                    for (i in 0 until next.childCount) {
                        children.add(next.getChildAt(i))
                    }
                }
            }
            return emptySet()
        }

    companion object {
        const val WINDOW_MANAGER_GLOBAL = "android.view.WindowManagerGlobal"
        const val WINDOW_MANAGER_GET_INSTANCE = "getInstance"
        const val WINDOW_MANAGER_VIEWS = "mViews"
        const val TAG = "AndroidCompositionExtractor"
        const val DEBUG = false
    }
}
