package com.decomposer.runtime.ir

import kotlinx.serialization.Serializable

@Serializable
enum class Variance {
    INVARIANT, IN_VARIANCE, OUT_VARIANCE
}
