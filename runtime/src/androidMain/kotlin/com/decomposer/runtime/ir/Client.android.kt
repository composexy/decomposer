package com.decomposer.runtime.ir

import com.decomposer.runtime.CommandHandler
import com.decomposer.runtime.Logger
import com.decomposer.runtime.connection.Client
import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.runtime.connection.model.DeviceDescriptor
import com.decomposer.runtime.connection.model.DeviceType

internal class AndroidClient(
    serverPort: Int = ConnectionContract.DEFAULT_SERVER_PORT,
    commandHandlers: Set<CommandHandler> = emptySet()
) : Client(serverPort, commandHandlers), Logger by AndroidLogger {
    override fun buildDeviceDescriptor(): DeviceDescriptor =
        DeviceDescriptor(deviceType = DeviceType.ANDROID)
}
