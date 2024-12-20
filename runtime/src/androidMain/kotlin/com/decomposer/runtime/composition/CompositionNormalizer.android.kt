package com.decomposer.runtime.composition

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.snapshots.SnapshotStateObserver
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import com.decomposer.runtime.Logger
import com.decomposer.runtime.connection.model.CompositionRoots
import com.decomposer.runtime.AndroidLogger
import com.decomposer.runtime.ViewReflection
import com.decomposer.runtime.WindowManagerReflection
import com.decomposer.runtime.compose.CompositionNormalizer

internal class AndroidCompositionNormalizer(
    private val context: Context
) : CompositionNormalizer(AndroidLogger), Logger by AndroidLogger  {

    private val uiDispatcher = AndroidUiDispatcher.Main
    private val frameClock = uiDispatcher[MonotonicFrameClock]
    private val compositions = mutableListOf<Composition>()
    private val snapshotStateObservers = mutableListOf<SnapshotStateObserver>()

    init { enableInspection() }

    override suspend fun extractCompositionRoots(): CompositionRoots {
        val clock = frameClock ?: throw IllegalArgumentException("Cannot find frame clock!")
        val reflection = WindowManagerReflection()
        return clock.withFrameNanos {
            val rootViews = reflection.rootViews
            extractCompositionData(rootViews)
        }
    }

    private fun extractCompositionData(rootViews: List<View>): CompositionRoots {
        compositions.clear()
        snapshotStateObservers.clear()
        rootViews.forEach { rootView ->
            val reflection = ViewReflection(rootView, AndroidLogger)
            reflection.composition?.let {
                compositions.add(it)
            }
            reflection.snapshotStateObserver?.let {
                snapshotStateObservers.add(it)
            }
        }
        return map(compositions, snapshotStateObservers)
    }

    private fun enableInspection() {
        isDebugInspectorInfoEnabled = true
    }
}
