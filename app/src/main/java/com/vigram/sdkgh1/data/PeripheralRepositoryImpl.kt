package com.vigram.sdkgh1.data

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.net.Uri
import com.vigram.sdkgh1.domain.repository.PeripheralRepository
import com.vigram.sdkgh1.data.mapper.toDeviceMethodSDK
import com.vigram.sdkgh1.domain.entity.MainDeviceMethod
import com.vigram.sdk.Models.Device.DeviceMessage
import com.vigram.sdk.Models.PeripheralState
import com.vigram.sdk.Models.Satellite.SatelliteMessage
import com.vigram.sdk.Models.Satellite.SatelliteMethod
import com.vigram.sdk.Models.StateUpdateSoftware
import com.vigram.sdk.Modules.NmeaMessage
import com.vigram.sdk.Peripheral.Peripheral
import com.vigram.sdk.Peripheral.StatePeripheralConfiguration
import com.vigram.sdk.Vigram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PeripheralRepositoryImpl(
    private val scope: CoroutineScope,
) : PeripheralRepository {

    @Volatile
    private var _peripheralService: Peripheral? = null

    override val peripheralService: Peripheral
        get() = _peripheralService
            ?: throw IllegalStateException("PeripheralService is not initialized")

    override fun connect(
        context: Context,
        bluetoothDevice: BluetoothDevice,
        uri: Uri,
        softwareFile: ByteArray?,
    ) {
        if (_peripheralService == null) {
            synchronized(this) {
                if (_peripheralService == null) {
                    val logger = Vigram.peripheralLogger(uri)

                    _peripheralService = Vigram.peripheral(
                        bluetoothDevice = bluetoothDevice,
                        logger = logger,
                        softwareFile = softwareFile
                    )

                    peripheralService.configurationState = {
                        scope.launch {
                            _configurationState.emit(it)
                        }
                    }

                    peripheralService.state = {
                        scope.launch {
                            _peripheralState.emit(it)
                        }
                    }

                    peripheralService.softwareUpdateState = {
                        scope.launch {
                            _softwareUpdateState.emit(it)
                        }
                    }

                    peripheralService.deviceMessages = {
                        scope.launch {
                            _deviceMessage.emit(it)
                        }
                    }

                    peripheralService.nmeaMessage = {
                        scope.launch {
                            _nmeaMessage.emit(it)
                        }
                    }

                    peripheralService.satelliteMessages = {
                        scope.launch {
                            _satelliteMessage.emit(it)
                        }
                    }

                    peripheralService.protocolVersion = {
                        scope.launch {
                            _protocolVersion.emit(it)
                        }
                    }
                }
            }
        }
    }

    override fun start() {
        peripheralService.start()
    }

    override fun send(byteArray: ByteArray) {
        peripheralService.send(byteArray)
    }

    override fun disconnect() {
        peripheralService.disconnect()
        _peripheralService = null
    }

    override fun deviceRequest(method: MainDeviceMethod) {
        peripheralService.deviceRequest(method.toDeviceMethodSDK())
    }

    override fun satelliteRequest(method: SatelliteMethod) =
        peripheralService.satelliteRequest(method)

    private val _peripheralState: MutableStateFlow<PeripheralState> =
        MutableStateFlow(PeripheralState.Disconnected)
    override val peripheralState: Flow<PeripheralState>
        get() = _peripheralState.asStateFlow()

    private val _softwareUpdateState: MutableStateFlow<StateUpdateSoftware> =
        MutableStateFlow(StateUpdateSoftware.None)
    override val softwareUpdateState: Flow<StateUpdateSoftware>
        get() = _softwareUpdateState.asStateFlow()

    private val _configurationState: MutableSharedFlow<StatePeripheralConfiguration> =
        MutableSharedFlow()
    override val configurationState: SharedFlow<StatePeripheralConfiguration>
        get() = _configurationState.asSharedFlow()

    private val _deviceMessage: MutableSharedFlow<DeviceMessage> = MutableSharedFlow()
    override val deviceMessage: Flow<DeviceMessage>
        get() = _deviceMessage.asSharedFlow()

    private val _nmeaMessage: MutableSharedFlow<NmeaMessage> = MutableSharedFlow()
    override val nmeaMessage: Flow<NmeaMessage>
        get() = _nmeaMessage.asSharedFlow()

    private val _satelliteMessage: MutableSharedFlow<SatelliteMessage> = MutableSharedFlow()
    override val satelliteMessage: Flow<SatelliteMessage>
        get() = _satelliteMessage.asSharedFlow()

    private val _protocolVersion: MutableSharedFlow<Double> = MutableSharedFlow()
    override val protocolVersion: SharedFlow<Double>
        get() = _protocolVersion.asSharedFlow()
}
