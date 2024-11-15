package com.decomposer.runtime.connection

import com.decomposer.runtime.connection.model.DeviceDescriptor

internal interface Client {
    fun start()
    fun stop()
    fun buildDeviceDescriptor(): DeviceDescriptor
}
