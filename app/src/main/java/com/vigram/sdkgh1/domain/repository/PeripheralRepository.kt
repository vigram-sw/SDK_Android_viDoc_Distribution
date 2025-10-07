package com.vigram.sdkgh1.domain.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.net.Uri
import com.vigram.sdkgh1.domain.entity.MainDeviceMethod
import com.vigram.sdk.Models.Device.DeviceMessage
import com.vigram.sdk.Models.PeripheralState
import com.vigram.sdk.Models.Satellite.SatelliteMessage
import com.vigram.sdk.Models.Satellite.SatelliteMethod
import com.vigram.sdk.Models.StateUpdateSoftware
import com.vigram.sdk.Modules.NmeaMessage
import com.vigram.sdk.Peripheral.Peripheral
import com.vigram.sdk.Peripheral.StatePeripheralConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface PeripheralRepository {

    val peripheralService: Peripheral

    fun connect(
        context: Context,
        bluetoothDevice: BluetoothDevice,
        uri: Uri,
        softwareFile: ByteArray?
    )

    fun start()

    fun send(byteArray: ByteArray)

    fun disconnect()

    fun deviceRequest(method: MainDeviceMethod)

    fun satelliteRequest(method: SatelliteMethod)

    val peripheralState: Flow<PeripheralState>

    val softwareUpdateState: Flow<StateUpdateSoftware>

    val configurationState: SharedFlow<StatePeripheralConfiguration>

    val deviceMessage: Flow<DeviceMessage>

    val nmeaMessage: Flow<NmeaMessage>

    val satelliteMessage: Flow<SatelliteMessage>

    val protocolVersion: SharedFlow<Double>
}