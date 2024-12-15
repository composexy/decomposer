package com.decomposer.runtime

import com.decomposer.runtime.compose.CompositionNormalizer
import com.decomposer.runtime.connection.AbstractOkHttpClient
import com.decomposer.runtime.connection.ConnectionContract
import com.decomposer.runtime.connection.model.DeviceDescriptor
import com.decomposer.runtime.connection.model.DeviceType
import com.decomposer.runtime.ir.ProjectScanner

internal class AndroidOkHttpClient(
    serverPort: Int = ConnectionContract.DEFAULT_SERVER_PORT,
    projectScanner: ProjectScanner,
    compositionNormalizer: CompositionNormalizer
) : AbstractOkHttpClient(
    serverPort,
    projectScanner,
    compositionNormalizer
), Logger by AndroidLogger {
    override fun buildDeviceDescriptor(): DeviceDescriptor =
        DeviceDescriptor(deviceType = DeviceType.ANDROID)
}
