package com.vigram.sdkgh1.compose.event

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

sealed class ScanDevicesEvent {
    data class Scan(val devices: StateFlow<List<BluetoothDevice>>) : ScanDevicesEvent()
    object Stop : ScanDevicesEvent()
}