package com.vigram.sdkgh1.domain.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.vigram.sdk.Bluetooth.BluetoothService
import kotlinx.coroutines.flow.StateFlow

interface BluetoothRepository {

    val bluetoothService: BluetoothService

    fun init(context: Context)

    fun startScan()

    fun stopScan()

    val isScan: StateFlow<Boolean>

    val devices: StateFlow<List<BluetoothDevice>>
}