package com.vigram.sdkgh1.data

import com.vigram.sdkgh1.domain.repository.ConfigurationRepository
import com.vigram.sdk.General.Configuration

object ConfigurationRepositoryImpl : ConfigurationRepository {

    override var debug: Boolean
        get() = Configuration.debug
        set(value) {
            Configuration.debug = value
        }
    override val sdkVersion: String
        get() = Configuration.SDK_VERSION

    override var blePacketSize: Int
        get() = Configuration.deviceBLEPacketSize
        set(value) {
            Configuration.deviceBLEPacketSize = value
        }

    override var bleWriteTimeout: Double
        get() = Configuration.bleWriteTimeout
        set(value) {
            Configuration.bleWriteTimeout = value
        }

    override var bleSendInterval: Long
        get() = Configuration.bleSendInterval
        set(value) {
            Configuration.bleSendInterval = value
        }

    override var softSendDelayByte: Long
        get() = Configuration.softwareSendDelay
        set(value) {
            Configuration.softwareSendDelay = value
        }
}