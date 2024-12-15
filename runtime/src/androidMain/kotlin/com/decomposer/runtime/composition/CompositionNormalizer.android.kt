package com.decomposer.runtime.composition

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.R
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import com.decomposer.runtime.Logger
import com.decomposer.runtime.connection.model.CompositionRoots
import com.decomposer.runtime.AndroidLogger
import com.decomposer.runtime.compose.CompositionNormalizer
import java.lang.reflect.Field
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMembers

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
@Suppress("UNCHECKED_CAST")
internal class AndroidCompositionNormalizer(
    private val context: Context
) : CompositionNormalizer(AndroidLogger), Logger by AndroidLogger  {

    private val uiDispatcher = AndroidUiDispatcher.Main
    private val frameClock = uiDispatcher[MonotonicFrameClock]
    private var windowManager: Any
    private var viewsField: Field
    private val compositions = mutableListOf<Composition>()

    init {
        enableInspection()
        val windowManagerClazz = Class.forName(WINDOW_MANAGER_GLOBAL)
        val getInstanceMethod = windowManagerClazz.getMethod(WINDOW_MANAGER_GET_INSTANCE)
        windowManager = getInstanceMethod.invoke(null)!!
        viewsField = windowManagerClazz.getDeclaredField(WINDOW_MANAGER_VIEWS).also {
            it.isAccessible = true
        }
    }

    override suspend fun extractCompositionRoots(): CompositionRoots {
        val clock = frameClock ?: throw IllegalArgumentException("Cannot find frame clock!")
        return clock.withFrameNanos {
            val rootViews = getAllRootViews()
            extractCompositionData(rootViews)
        }
    }

    private fun extractCompositionData(rootViews: List<View>): CompositionRoots {
        compositions.clear()
        rootViews.forEach { rootView ->
            rootView.composition?.let {
                compositions.add(it)
            }
        }
        return map(compositions)
    }

    private fun getAllRootViews(): List<View> {
        return if (Build.VERSION.SDK_INT >= 29) {
            WindowInspector.getGlobalWindowViews()
        } else {
            viewsField.get(windowManager) as List<View>
        }
    }

    private fun enableInspection() {
        isDebugInspectorInfoEnabled = true
    }

    private val View.composition: Composition?
        get() {
            val children = mutableListOf(this)
            while (children.isNotEmpty()) {
                val next = children.last()
                if (next.getTag(R.id.wrapped_composition_tag) != null) {
                    val wrappedComposition = next.getTag(R.id.wrapped_composition_tag)
                    val clazz = wrappedComposition::class
                    val property = clazz.declaredMembers
                        .find { it.name == WRAPPED_COMPOSITION_ORIGINAL } as? KProperty1<Any, *>
                    if (property == null) {
                        log(Logger.Level.WARNING, TAG, "Cannot find original property!")
                        return null
                    }
                    return property.get(wrappedComposition) as Composition
                }
                children.remove(next)
                if (next is ViewGroup) {
                    for (i in 0 until next.childCount) {
                        children.add(next.getChildAt(i))
                    }
                }
            }
            return null
        }

    companion object {
        private const val WINDOW_MANAGER_GLOBAL = "android.view.WindowManagerGlobal"
        private const val WRAPPED_COMPOSITION_ORIGINAL = "original"
        private const val WINDOW_MANAGER_GET_INSTANCE = "getInstance"
        private const val WINDOW_MANAGER_VIEWS = "mViews"
        private const val TAG = "AndroidCompositionExtractor"
    }
}
