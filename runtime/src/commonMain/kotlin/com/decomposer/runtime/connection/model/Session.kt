package com.decomposer.runtime.connection.model

import kotlinx.serialization.Serializable

@Serializable
class Session(
    val sessionId: String,
    val sessionUrl: String
)
