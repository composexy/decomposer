package com.decomposer.runtime.connection.model

import kotlinx.serialization.Serializable

@Serializable
class DeviceDescriptor(
    val deviceType: DeviceType,
    val serialNumber: String
)

@Serializable
enum class DeviceType {
    ANDROID, DESKTOP
}
