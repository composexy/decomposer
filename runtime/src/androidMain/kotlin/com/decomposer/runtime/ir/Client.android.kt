package com.decomposer.runtime.ir

import com.decomposer.runtime.connection.Client
import com.decomposer.runtime.connection.model.DeviceDescriptor
import com.decomposer.runtime.connection.model.DeviceType

class AndroidClient(serverPort: Int) : Client(serverPort) {
    override fun buildDeviceDescriptor(): DeviceDescriptor =
        DeviceDescriptor(
            deviceType = DeviceType.ANDROID,
            serialNumber = ""
        )
}
