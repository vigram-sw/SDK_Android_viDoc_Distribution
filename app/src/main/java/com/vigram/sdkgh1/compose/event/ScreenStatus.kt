package com.vigram.sdkgh1.compose.event

sealed class ScreenStatus {
    data class Authentication(val status: AuthenticationEvent) : ScreenStatus()
    data class ScanDevices(val scanDevicesStatus: ScanDevicesEvent) : ScreenStatus()
    data class Peripheral(val status: PeripheralEvent) : ScreenStatus()
    data class Configuration(val configurationStatus: ConfigurationEvent) : ScreenStatus()
    data class SoftwareUpdate(val softwareUpdateStatus: SoftwareUpdateEvent) : ScreenStatus()
}