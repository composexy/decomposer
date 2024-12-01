package com.decomposer.runtime.connection.model

import kotlinx.serialization.Serializable

@Serializable
class DeviceDescriptor(
    val deviceType: DeviceType
)

@Serializable
enum class DeviceType {
    ANDROID
}
