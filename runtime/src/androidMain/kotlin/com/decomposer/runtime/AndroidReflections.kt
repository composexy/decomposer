package com.decomposer.runtime

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector
import androidx.compose.runtime.Composition
import androidx.compose.runtime.snapshots.SnapshotStateObserver
import androidx.compose.ui.R
import java.lang.reflect.Field
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
@Suppress("UNCHECKED_CAST")
internal class WindowManagerReflection {
    private var windowManager: Any?
    private var viewsField: Field

    init {
        val windowManagerClazz = Class.forName(WINDOW_MANAGER_GLOBAL)
        val getInstanceMethod = windowManagerClazz.getMethod(WINDOW_MANAGER_GET_INSTANCE)
        windowManager = getInstanceMethod.invoke(null)!!
        viewsField = windowManagerClazz.getDeclaredField(WINDOW_MANAGER_VIEWS).also {
            it.isAccessible = true
        }
    }

    val rootViews: List<View>
        get() {
            return if (Build.VERSION.SDK_INT >= 29) {
                WindowInspector.getGlobalWindowViews()
            } else {
                viewsField.get(windowManager) as List<View>
            }
        }

    companion object {
        private const val WINDOW_MANAGER_GLOBAL = "android.view.WindowManagerGlobal"
        private const val WINDOW_MANAGER_GET_INSTANCE = "getInstance"
        private const val WINDOW_MANAGER_VIEWS = "mViews"
        private const val TAG = "WindowManagerReflection"
    }
}

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
@Suppress("UNCHECKED_CAST")
internal class ViewReflection(
    private val view: View,
    private val logger: Logger
) {
    val composition: Composition?
        get() {
            val children = mutableListOf(view)
            while (children.isNotEmpty()) {
                val next = children.last()
                if (next.getTag(R.id.wrapped_composition_tag) != null) {
                    val wrappedComposition = next.getTag(R.id.wrapped_composition_tag)
                    val clazz = wrappedComposition::class
                    val property = clazz.declaredMembers
                        .find { it.name == WRAPPED_COMPOSITION_ORIGINAL } as? KProperty1<Any, *>
                    if (property == null) {
                        logger.log(Logger.Level.WARNING, TAG, "Cannot find original property!")
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

    val snapshotStateObserver: SnapshotStateObserver?
        get() {
            val children = mutableListOf(view)
            while (children.isNotEmpty()) {
                val next = children.last()
                val viewName = next::class.qualifiedName
                if (viewName == "androidx.compose.ui.platform.AndroidComposeView") {
                    val viewClazz = next::class
                    val ownerObserverProperty = viewClazz.members
                        .find { it.name == SNAPSHOT_OBSERVER } as? KProperty1<Any, *>
                    if (ownerObserverProperty == null) {
                        logger.log(Logger.Level.WARNING, TAG, "Cannot find snapshotObserver property!")
                        return null
                    }
                    ownerObserverProperty.isAccessible = true
                    val ownerObserver = ownerObserverProperty.get(next)
                    if (ownerObserver == null) {
                        logger.log(Logger.Level.WARNING, TAG, "Cannot get owner observer!")
                        return null
                    }
                    val ownerClazz = ownerObserver::class
                    val observerProperty = ownerClazz.members
                        .find { it.name == OBSERVER } as? KProperty1<Any, *>
                    if (observerProperty == null) {
                        logger.log(Logger.Level.WARNING, TAG, "Cannot find observer property!")
                        return null
                    }
                    observerProperty.isAccessible = true
                    val snapshotObserver = observerProperty.get(ownerObserver) as? SnapshotStateObserver
                    if (snapshotObserver == null) {
                        logger.log(Logger.Level.WARNING, TAG, "Cannot get snapshot observer!")
                    }
                    return snapshotObserver
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
        private const val WRAPPED_COMPOSITION_ORIGINAL = "original"
        private const val SNAPSHOT_OBSERVER = "snapshotObserver"
        private const val OBSERVER = "observer"
        private const val TAG = "ViewReflection"
    }
}
