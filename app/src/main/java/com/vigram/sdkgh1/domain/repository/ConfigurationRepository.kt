package com.vigram.sdkgh1.domain.repository

interface ConfigurationRepository {

    var debug: Boolean

    val sdkVersion: String

    var blePacketSize: Int

    var bleWriteTimeout: Double

    var bleSendInterval: Long

    var softSendDelayByte: Long
}