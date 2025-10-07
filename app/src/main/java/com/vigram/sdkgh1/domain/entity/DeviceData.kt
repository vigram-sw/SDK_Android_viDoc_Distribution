package com.vigram.sdkgh1.domain.entity

data class DeviceData(
    val sdkVersion: String = "-",
    val blePacketSize: String = "-",
    val bleWriteTimeout: String = "-",
    val bleSendInterval: String = "-",
    val softwareSendDelay: String = "-",
    val ntripSendInterval: String = "-",
    val nameDevice: String = "-",
    val protocolVersion: String = "-",
    val serialNumber: String = "-",
    val deviceNumber: String = "-",
    val hasFrontLaser: Boolean = false,
    val hasBottomLaser: Boolean = false,
    val hasIMU: Boolean = false,
    val hasCalibrated: Boolean = false,
    val housing: String = "-",
    val mount: String = "-",
    val hwRef: String = "-",
    val hwBat: String = "-",
    val currentDeviceType: String = "-",
    val hardware: String = "-",
    val software: String = "-",
    val battery: String = "-",
    val connectionStatus: Boolean = false
)