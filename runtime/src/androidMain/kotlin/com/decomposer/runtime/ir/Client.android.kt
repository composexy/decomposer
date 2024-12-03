package com.decomposer.runtime.ir

import com.decomposer.runtime.Logger
import com.decomposer.runtime.compose.CompositionExtractor
import com.decomposer.runtime.connection.AbstractOkHttpClient
import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.runtime.connection.model.DeviceDescriptor
import com.decomposer.runtime.connection.model.DeviceType

internal class AndroidOkHttpClient(
    serverPort: Int = ConnectionContract.DEFAULT_SERVER_PORT,
    projectScanner: ProjectScanner,
    compositionExtractor: CompositionExtractor
) : AbstractOkHttpClient(
    serverPort,
    projectScanner,
    compositionExtractor
), Logger by AndroidLogger {
    override fun buildDeviceDescriptor(): DeviceDescriptor =
        DeviceDescriptor(deviceType = DeviceType.ANDROID)
}
