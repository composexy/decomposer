package com.decomposer.runtime.connection.model

import kotlinx.serialization.Serializable

@Serializable
class SessionData(
    val sessionId: String,
    val sessionUrl: String
)
