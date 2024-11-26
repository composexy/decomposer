package com.decomposer.runtime.compose

import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.tooling.CompositionData
import com.decomposer.runtime.Logger
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

@Suppress("UNCHECKED_CAST")
class CompositionReflection(
    private val composition: Composition,
    private val logger: Logger
) {
    val parent: CompositionContext?
        get() {
            if (this::class.qualifiedName == COMPOSITION_IMPL) {
                val compositionImplClazz = composition::class
                val parentProperty = compositionImplClazz.declaredMembers
                    .find { it.name == COMPOSITION_IMPL_PARENT } as? KProperty1<Any, *>
                if (parentProperty == null) {
                    logger.log(Logger.Level.WARNING, TAG, "Cannot find parent property!")
                    return null
                }
                return parentProperty.get(composition) as CompositionContext?
            } else {
                logger.log(Logger.Level.WARNING, TAG, "Unknown composition type: $this")
                return null
            }
        }

    val observations: Map<Any, Set<Any>>
        get() {
            val compositionImplClazz = composition::class
            val observationsProperty = compositionImplClazz.declaredMembers
                .find { it.name == COMPOSITION_IMPL_OBSERVATIONS } as? KProperty1<Any, *>
            if (observationsProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find observations property!")
                return emptyMap()
            }
            val observations = observationsProperty.get(composition)
            if (observations == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find get observations!")
                return emptyMap()
            }
            val asMapMethod = observations::class.declaredFunctions
                .find { it.name == SCOPE_MAP_AS_MAP }
            if (asMapMethod == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find asMap method!")
                return emptyMap()
            }
            asMapMethod.isAccessible = true
            return asMapMethod.call(observations) as Map<Any, Set<Any>>
        }

    val compositionData: CompositionData?
        get() {
            val compositionImplClazz = composition::class
            val slotTableProperty = compositionImplClazz.declaredMembers
                .find { it.name == COMPOSITION_IMPL_SLOT_TABLE } as? KProperty1<Any, *>
            if (slotTableProperty == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find slotTable property!")
                return null
            }
            val slotTable = slotTableProperty.get(composition)
            if (slotTable == null) {
                logger.log(Logger.Level.WARNING, TAG, "Cannot find get slotTable!")
                return null
            }
            return slotTable as? CompositionData?
        }

    companion object {
        private const val COMPOSITION_IMPL = "androidx.compose.runtime.CompositionImpl"
        private const val COMPOSITION_IMPL_OBSERVATIONS = "observations"
        private const val COMPOSITION_IMPL_PARENT = "parent"
        private const val COMPOSITION_IMPL_SLOT_TABLE = "slotTable"
        private const val SCOPE_MAP_AS_MAP = "asMap"
        private const val TAG = "CompositionReflection"
    }
}
