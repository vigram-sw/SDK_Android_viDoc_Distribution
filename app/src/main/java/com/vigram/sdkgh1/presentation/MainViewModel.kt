package com.vigram.sdkgh1

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vigram.sdk.Authentication.AuthenticationResult
import com.vigram.sdk.Laser.LaserConfiguration
import com.vigram.sdk.Models.Device.DeviceMessage
import com.vigram.sdk.Models.Device.LaserState
import com.vigram.sdk.Models.GGAMessage
import com.vigram.sdk.Models.NtripState
import com.vigram.sdk.Models.PeripheralState
import com.vigram.sdk.Models.RequestResult
import com.vigram.sdk.Models.Satellite.DynamicStateType
import com.vigram.sdk.Models.Satellite.SatelliteMessage
import com.vigram.sdk.Models.Satellite.SatelliteMessage.ChangingRate
import com.vigram.sdk.Models.Satellite.SatelliteMessage.ElevationMessage
import com.vigram.sdk.Models.Satellite.SatelliteMessage.StatusSattelite
import com.vigram.sdk.Models.Satellite.SatelliteMethod
import com.vigram.sdk.Models.Satellite.SatelliteMethod.ChangeStatusGNSS
import com.vigram.sdk.Models.SinglePoint
import com.vigram.sdk.Models.StateUpdateSoftware
import com.vigram.sdk.Modules.NmeaMessage
import com.vigram.sdk.NTRIP.NtripMountPoint
import com.vigram.sdk.Peripheral.StatePeripheralConfiguration
import com.vigram.sdk.SinglePoint.SinglePointRecordModel
import com.vigram.sdkgh1.compose.event.AuthenticationEvent
import com.vigram.sdkgh1.compose.event.ChangingRateUiEvent
import com.vigram.sdkgh1.compose.event.ConfigurationEvent
import com.vigram.sdkgh1.compose.event.ElevationUiEvent
import com.vigram.sdkgh1.compose.event.GnssUiEvent
import com.vigram.sdkgh1.compose.event.LaserUiEvent
import com.vigram.sdkgh1.compose.event.PeripheralEvent
import com.vigram.sdkgh1.compose.event.ScanDevicesEvent
import com.vigram.sdkgh1.compose.event.ScreenStatus
import com.vigram.sdkgh1.compose.event.SoftwareUpdateEvent
import com.vigram.sdkgh1.compose.scripts.DynamicStateListener
import com.vigram.sdkgh1.data.AuthenticationRepositoryImpl
import com.vigram.sdkgh1.data.BluetoothRepositoryImpl
import com.vigram.sdkgh1.data.ConfigurationRepositoryImpl
import com.vigram.sdkgh1.data.LaserRepositoryImpl
import com.vigram.sdkgh1.data.NtripRepositoryImpl
import com.vigram.sdkgh1.data.PeripheralRepositoryImpl
import com.vigram.sdkgh1.data.SinglePointRepositoryImpl
import com.vigram.sdkgh1.data.mapper.toGgaSDK
import com.vigram.sdkgh1.data.mapper.toGstSDK
import com.vigram.sdkgh1.data.mapper.toNtripConnectionInformationSDK
import com.vigram.sdkgh1.domain.entity.DeviceData
import com.vigram.sdkgh1.domain.entity.GgaData
import com.vigram.sdkgh1.domain.entity.GstData
import com.vigram.sdkgh1.domain.entity.MainDeviceMethod
import com.vigram.sdkgh1.domain.entity.NtripConnectionData
import com.vigram.sdkgh1.domain.repository.AuthenticationRepository
import com.vigram.sdkgh1.domain.repository.BluetoothRepository
import com.vigram.sdkgh1.domain.repository.ConfigurationRepository
import com.vigram.sdkgh1.domain.repository.LaserRepository
import com.vigram.sdkgh1.domain.repository.NtripRepository
import com.vigram.sdkgh1.domain.repository.PeripheralRepository
import com.vigram.sdkgh1.helpers.createFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val application: Application,
    private val repositoryConfiguration: ConfigurationRepository = ConfigurationRepositoryImpl,
) : ViewModel(), DynamicStateListener {

    private val repositoryPeripheral: PeripheralRepository =
        PeripheralRepositoryImpl(viewModelScope)
    private val repositoryAuthentication: AuthenticationRepository = AuthenticationRepositoryImpl
    private val repositoryBluetooth: BluetoothRepository = BluetoothRepositoryImpl(viewModelScope)
    private val repositoryNtrip: NtripRepository = NtripRepositoryImpl(viewModelScope)
    private val repositoryLaser: LaserRepository = LaserRepositoryImpl(viewModelScope)
    private val repositorySinglePoint: SinglePointRepositoryImpl = SinglePointRepositoryImpl

    private var _deviceData: MutableStateFlow<DeviceData> = MutableStateFlow(DeviceData())
    val deviceData: StateFlow<DeviceData> = _deviceData.asStateFlow()

    private var _ggaMessage: MutableStateFlow<GgaData> = MutableStateFlow(GgaData())
    val ggaMessage: StateFlow<GgaData> get() = _ggaMessage.asStateFlow()

    private var _gstMessage: MutableStateFlow<GstData> = MutableStateFlow(GstData())
    val gstMessage: StateFlow<GstData> get() = _gstMessage.asStateFlow()

    private var _txtMessage: MutableStateFlow<String> = MutableStateFlow("")
    val txtMessage: StateFlow<String> get() = _txtMessage.asStateFlow()


    private var _screenStatus: MutableStateFlow<ScreenStatus> =
        MutableStateFlow(ScreenStatus.Authentication(AuthenticationEvent.Start))
    val screenStatus: StateFlow<ScreenStatus> get() = _screenStatus.asStateFlow()


    init {
        repositoryConfiguration.debug = true
    }

    fun authentication(token: String) {
        repositoryAuthentication.init(context = application.applicationContext, token = token)

        repositoryAuthentication.check()
            .onStart { _screenStatus.emit(ScreenStatus.Authentication(AuthenticationEvent.Start)) }
            .onEach { result ->
                when (result) {
                    is AuthenticationResult.Error -> {
                        _screenStatus.emit(ScreenStatus.Authentication(AuthenticationEvent.Error("failed")))
                    }

                    is AuthenticationResult.Success -> {
                        repositoryBluetooth.init(application)
                        _screenStatus.emit(ScreenStatus.Authentication(AuthenticationEvent.Success))
                        delay(500)
                        _screenStatus.emit(ScreenStatus.ScanDevices(ScanDevicesEvent.Stop))
                    }
                }
            }
            .launchIn(viewModelScope)
    }


    fun startScan() {
        viewModelScope.launch {
            _screenStatus.emit(
                ScreenStatus.ScanDevices(ScanDevicesEvent.Scan(repositoryBluetooth.devices))
            )
            repositoryBluetooth.startScan()
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            _screenStatus.emit(
                ScreenStatus.ScanDevices(ScanDevicesEvent.Stop)
            )
            repositoryBluetooth.stopScan()
        }
    }

    private var _softwareFile: MutableStateFlow<ByteArray> = MutableStateFlow(byteArrayOf())
    val softwareFile: StateFlow<ByteArray> get() = _softwareFile.asStateFlow()

    fun softwareFile(softwareFile: ByteArray) {
        _softwareFile.value = softwareFile
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectDevice(device: BluetoothDevice) {
        stopScan()

        val uri = createFile(device.name, "LOG", "txt").toUri()

        repositoryPeripheral.connect(
            application.applicationContext,
            bluetoothDevice = device,
            uri = uri,
            softwareFile = softwareFile.value
        )

        viewModelScope.launch {
            repositoryPeripheral.start()
        }

        repositoryPeripheral.peripheralState
            .onEach {
                if (it == PeripheralState.Connected) {
                    _screenStatus.emit(ScreenStatus.Peripheral(PeripheralEvent.Connected))
                    _deviceData.emit(deviceData.value.copy(connectionStatus = true))
                    repositoryLaser.init(repositoryPeripheral.peripheralService)
                }

                if (it == PeripheralState.Disconnected) {
                    defaultParams()

                    _screenStatus.emit(ScreenStatus.Peripheral(PeripheralEvent.Disconnected))
                    _screenStatus.emit(ScreenStatus.ScanDevices(ScanDevicesEvent.Stop))

                    _deviceData.emit(deviceData.value.copy(connectionStatus = false))
                }
            }
            .launchIn(viewModelScope)

        repositoryPeripheral.softwareUpdateState
            .onEach {
                when (it) {
                    StateUpdateSoftware.End -> {
                        _screenStatus.emit(ScreenStatus.SoftwareUpdate(SoftwareUpdateEvent.End))
                        delay(2000)
                        disconnectDevice()
                    }

                    StateUpdateSoftware.Error ->
                        _screenStatus.emit(
                            ScreenStatus.SoftwareUpdate(
                                SoftwareUpdateEvent.Error("Error update")
                            )
                        )

                    StateUpdateSoftware.None ->
                        _screenStatus.emit(
                            ScreenStatus.SoftwareUpdate(
                                SoftwareUpdateEvent.None
                            )
                        )

                    is StateUpdateSoftware.Updating ->
                        _screenStatus.emit(
                            ScreenStatus.SoftwareUpdate(
                                SoftwareUpdateEvent.Updating(it.progress)
                            )
                        )

                    is StateUpdateSoftware.Start ->
                        _screenStatus.emit(
                            ScreenStatus.SoftwareUpdate(
                                SoftwareUpdateEvent.Updating(0.0)
                            )
                        )
                }


            }
            .launchIn(viewModelScope)

//            Log.e("DeviceData2", deviceData.value.toString())

        _deviceData.update {
            it.copy(
                sdkVersion = repositoryConfiguration.sdkVersion,
                blePacketSize = repositoryConfiguration.blePacketSize.toString(),
                bleWriteTimeout = repositoryConfiguration.bleWriteTimeout.toString(),
                bleSendInterval = repositoryConfiguration.bleSendInterval.toString(),
                softwareSendDelay = repositoryConfiguration.softSendDelayByte.toString(),
                nameDevice = device.name
            )
        }

        repositoryPeripheral.protocolVersion
            .onEach { version ->
                _deviceData.update { it.copy(protocolVersion = version.toString()) }
            }
            .launchIn(viewModelScope)

        repositoryPeripheral.deviceMessage
            .onEach { deviceMessage ->
//                    Log.d("ViewModel", "DeviceMessage: $deviceMessage")
                when (deviceMessage) {
                    is DeviceMessage.Battery ->
                        _deviceData.update { it.copy(battery = "${deviceMessage.percentage}%") }

                    is DeviceMessage.LaserStateInfo -> {
                        Log.d("ViewModel", "LaserState: ${deviceMessage.value}")
                    }

                    is DeviceMessage.Measurement -> {}

                    is DeviceMessage.Version -> {
                        _deviceData.update {
                            it.copy(
                                software = deviceMessage.software.toString(),
                                hardware = deviceMessage.hardware.toString()
                            )
                        }
                    }

                    is DeviceMessage.Device -> {
                        _deviceData.update {
                            it.copy(
                                housing = deviceMessage.value.housing?.value ?: "-",
                                hasIMU = deviceMessage.value.hasIMU,
                                mount = deviceMessage.value.mount?.value ?: "-",
                                hasCalibrated = deviceMessage.value.hasCalibrated,
                                hasFrontLaser = deviceMessage.value.hasFrontLaser,
                                hasBottomLaser = deviceMessage.value.hasBottomLaser
                            )
                        }

                    }

                    is DeviceMessage.HardwareIndex -> {}
                    is DeviceMessage.HardwareInfo -> {}
                    is DeviceMessage.ImuACC -> {}
                    is DeviceMessage.ImuAngle -> {}
                    is DeviceMessage.ImuCalibrationStatus -> {}
                    is DeviceMessage.ImuMagneticRaw -> {}
                    is DeviceMessage.ImuRotation -> {}
                    is DeviceMessage.ImuRotationRaw -> {}
                    is DeviceMessage.ImuTemp -> {}
                    is DeviceMessage.SerialNumber -> {
                        _deviceData.update { it.copy(serialNumber = deviceMessage.serialNumber) }
                    }

                    is DeviceMessage.SwitchProtocolAnswer -> {}
                }
            }
            .launchIn(viewModelScope)

        repositoryPeripheral.nmeaMessage
            .onEach {
                when (it) {
                    is NmeaMessage.GNGGA -> {
                        gga = it.value
                        _ggaMessage.emit(it.value.toGgaSDK())
                    }

                    is NmeaMessage.GNGST -> _gstMessage.emit(it.value.toGstSDK())
                    is NmeaMessage.TXT -> _txtMessage.emit(it.value.raw)
                }
            }
            .launchIn(viewModelScope)

        repositoryPeripheral.configurationState.onEach {
            when (it) {
                StatePeripheralConfiguration.Done -> {
                    _screenStatus.emit(ScreenStatus.Configuration(ConfigurationEvent.Done))
                    delay(500)
                    _screenStatus.emit(ScreenStatus.Peripheral(PeripheralEvent.Connected))
                }

                is StatePeripheralConfiguration.Error ->
                    _screenStatus.emit(ScreenStatus.Configuration(ConfigurationEvent.Error("Error")))

                is StatePeripheralConfiguration.Failed ->
                    _screenStatus.emit(ScreenStatus.Configuration(ConfigurationEvent.Error("Failed")))

                is StatePeripheralConfiguration.InProgress ->
                    _screenStatus.emit(
                        ScreenStatus.Configuration(ConfigurationEvent.InProgress(it.message))
                    )
            }
        }.launchIn(viewModelScope)


    }

    private var gga: GGAMessage? = null

    fun dialogConfigurationRead() {
        viewModelScope.launch {
            _screenStatus.emit(ScreenStatus.Peripheral(PeripheralEvent.Connected))
        }
    }

    fun disconnectDevice() {
        repositoryPeripheral.disconnect()
    }

    fun deviceRequest(method: MainDeviceMethod) = viewModelScope.launch {
        repositoryPeripheral.deviceRequest(method)
    }

    fun satelliteRequest(method: SatelliteMethod) = viewModelScope.launch {
        repositoryPeripheral.satelliteRequest(method)
    }

    fun changeBlePacketSize(size: String) {
        val size = size.toIntOrNull() ?: run {
            _deviceData.update { it.copy(blePacketSize = "") }
            return
        }

        repositoryConfiguration.blePacketSize = size
        _deviceData.update { it.copy(blePacketSize = size.toString()) }
    }

    fun changeBleWriteTimeout(delay: String) {
        val delay = delay.toDoubleOrNull() ?: run {
            _deviceData.update { it.copy(bleWriteTimeout = "") }
            return
        }
        repositoryConfiguration.bleWriteTimeout = delay
        _deviceData.update { it.copy(bleWriteTimeout = delay.toString()) }
    }

    fun changeBleSendInterval(delay: String) {
        val delay = delay.toLongOrNull() ?: run {
            _deviceData.update { it.copy(bleSendInterval = "") }
            return
        }
        repositoryConfiguration.bleSendInterval = delay
        _deviceData.update { it.copy(bleSendInterval = delay.toString()) }
    }

    fun changeSoftwareSendDelay(delay: String) {
        val delay = delay.toLongOrNull() ?: run {
            _deviceData.update { it.copy(softwareSendDelay = "") }
            return
        }
        repositoryConfiguration.softSendDelayByte = delay
        _deviceData.update { it.copy(softwareSendDelay = delay.toString()) }
    }


//    private val _laserStatus = MutableSharedFlow<RequestResult<*>>()
//    val laserStatus: SharedFlow<RequestResult<*>>
//        get() = _laserStatus.asSharedFlow()

    ///LASER
    private val _laserStatus = MutableSharedFlow<RequestResult<LaserState>>(replay = 1)
    val laserStatus: SharedFlow<RequestResult<LaserState>> get() = _laserStatus.asSharedFlow()
    private val _laserMeasurement =
        MutableSharedFlow<RequestResult<DeviceMessage.Measurement>>(replay = 1)
    val laserMeasurement: SharedFlow<RequestResult<DeviceMessage.Measurement>>
        get() = _laserMeasurement.asSharedFlow()

    fun laserEvent(event: LaserUiEvent) {
        when (event) {
            is LaserUiEvent.LaserOff -> {
                repositoryLaser.laserOff(event.laserPosition)
            }

            is LaserUiEvent.LaserOn -> {
                repositoryLaser.laserOn(event.laserPosition)
            }

            LaserUiEvent.LaserStatus -> repositoryLaser.getLaserStatus()
            is LaserUiEvent.Measurement -> {
                repositoryLaser.startLaserMeasurement(event.laserPosition)
                    .onEach {
                        _laserMeasurement.emit(it)
                    }.launchIn(viewModelScope)
            }
        }
    }


    ////SINGLE POINT

    private val _singlePointMeasurementResult =
        MutableSharedFlow<RequestResult<SinglePointRecordModel<SinglePoint>>>()
    val singlePointMeasurementResult: SharedFlow<RequestResult<SinglePointRecordModel<SinglePoint>>>
        get() = _singlePointMeasurementResult.asSharedFlow()

    fun singlePointMeassurement(laserConfiguration: LaserConfiguration) = viewModelScope.launch {
//        repositorySinglePoint.startMeasurement(laserConfiguration)
//            .collectLatest { _singlePointMeasurementResult.emit(it) }
    }


    /////NTRIP
    private val _listNtripInfo = MutableStateFlow(listOf<NtripConnectionData>())
    val listNtripInfo: StateFlow<List<NtripConnectionData>> = _listNtripInfo.asStateFlow()

    private val _ntripInfo = MutableStateFlow(NtripConnectionData.empty())
    val ntripInfo: StateFlow<NtripConnectionData> = _ntripInfo.asStateFlow()

    fun updateHost(value: String) {
        _ntripInfo.update { it.copy(host = value) }
    }

    fun updatePort(value: String) {
        _ntripInfo.update { it.copy(port = value) }
    }

    fun updateMount(value: String) {
        _ntripInfo.update { it.copy(mount = value) }
    }

    fun updateLogin(value: String) {
        _ntripInfo.update { it.copy(login = value) }
    }

    fun updatePassword(value: String) {
        _ntripInfo.update { it.copy(password = value) }
    }

    fun updateNtripInfo(value: NtripConnectionData) {
        _ntripInfo.value = value
    }

    val ntripData: StateFlow<ByteArray> = repositoryNtrip.data

    private val _mountPoints = MutableStateFlow<List<NtripMountPoint>>(emptyList())
    val mountPoints: StateFlow<List<NtripMountPoint>> = _mountPoints.asStateFlow()

    private val _loadingMounts = MutableStateFlow(false)
    val loadingMounts: StateFlow<Boolean> = _loadingMounts.asStateFlow()

    private var mountsJob: Job? = null

    val isMountsConnecting = MutableStateFlow(false)
    fun fetchMounts() {

        if (ntripInfo.value.host == "") return
        if (ntripInfo.value.port == "") return

        _loadingMounts.value = true

        if (isMountsConnecting.value) return
        isMountsConnecting.value = true

        mountsJob?.cancel()

        mountsJob = viewModelScope.launch {
            repositoryNtrip.mountpoints(ntripInfo.value.toNtripConnectionInformationSDK())
                .catch {
                    Log.e("FetchMountsCatch", it.localizedMessage ?: it.message.toString())
                }
                .collect { result ->

                    when (result) {
                        is RequestResult.Success -> {
                            _mountPoints.value = result.value
                        }

                        is RequestResult.Error -> {

                        }
                    }
                    _loadingMounts.value = false
                    isMountsConnecting.value = false
                }
        }
    }

    fun fetchMountsCancel() {
        mountsJob?.cancel()
    }


    private var _ntripState: MutableStateFlow<NtripState> =
        MutableStateFlow(NtripState.Disconnected)
    val ntripState: StateFlow<NtripState> get() = _ntripState

    fun ntripConnect() = viewModelScope.launch {
        gga?.let {
            repositoryNtrip.startTask(
                repositoryPeripheral.peripheralService,
                ntripInfo.value,
                it
            )

//            repositorySinglePoint.init(
//                repositoryPeripheral.peripheralService,
//                repositoryNtrip.ntripTask
//            )
//
            repositoryNtrip.ntripState
                .onEach { _ntripState.emit(it) }
                .launchIn(viewModelScope)
        }
    }

    fun ntripDisconnect() {
        viewModelScope.launch {
            repositoryNtrip.disconnect()
        }
    }

    //Sattelite comands
    override val state: StateFlow<String>
        get() = repositoryPeripheral.satelliteMessage
            .filterIsInstance<SatelliteMessage.DynamicState>()
            .map { it.value.name }
            .onEach { it }
            .stateIn(viewModelScope, SharingStarted.Lazily, "Unknown")

    override fun onCurrentDynamicState() {
        viewModelScope.launch {
            repositoryPeripheral.satelliteRequest(method = SatelliteMethod.GetDynamicState)
        }
    }

    override fun onSetDynamicState(dynamicStateValue: DynamicStateType) {
        viewModelScope.launch {
            repositoryPeripheral.satelliteRequest(
                method = SatelliteMethod.SetDynamicState(dynamicStateValue, false)
            )
        }
    }

    ///
    val stateSatellite: SharedFlow<StatusSattelite>
        get() = repositoryPeripheral.satelliteMessage
            .filterIsInstance<StatusSattelite>()
            .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)


    fun satelliteStatusEvent(event: GnssUiEvent) {
        viewModelScope.launch {
            when (event) {
                is GnssUiEvent.ChangeSatelliteStatus -> {
                    repositoryPeripheral.satelliteRequest(
                        method = ChangeStatusGNSS(event.value, event.status)
                    )
                }

                is GnssUiEvent.GetSatelliteStatus -> {
                    repositoryPeripheral.satelliteRequest(
                        method = SatelliteMethod.GetCurrentStatusGNSS(event.value)
                    )
                }

                GnssUiEvent.ActivateAllSatellite ->
                    repositoryPeripheral.satelliteRequest(SatelliteMethod.ActivateAllConstellationGNSS)
            }
        }
    }

    val stateElevation: SharedFlow<ElevationMessage>
        get() = repositoryPeripheral.satelliteMessage
            .filterIsInstance<ElevationMessage>()
            .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    fun elevationControl(elevationUIEvent: ElevationUiEvent) {
        viewModelScope.launch {
            when (elevationUIEvent) {
                is ElevationUiEvent.ChangeElevation ->
                    repositoryPeripheral.satelliteRequest(
                        SatelliteMethod.SetMinimumElevation(
                            elevationUIEvent.value
                        )
                    )

                ElevationUiEvent.GetElevation ->
                    repositoryPeripheral.satelliteRequest(SatelliteMethod.GetCurrentMinimumElevation)
            }
        }
    }

    val stateChangingRate: SharedFlow<ChangingRate>
        get() = repositoryPeripheral.satelliteMessage
            .filterIsInstance<ChangingRate>()
            .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    fun changingRateControl(changingRateUIEvent: ChangingRateUiEvent) {
        viewModelScope.launch {
            when (changingRateUIEvent) {
                is ChangingRateUiEvent.ChangeChangingRate ->
                    repositoryPeripheral.satelliteRequest(
                        SatelliteMethod.SetChangingRateOfMessages(
                            changingRateUIEvent.value,
                            false
                        )
                    )

                ChangingRateUiEvent.GetChangingRate ->
                    repositoryPeripheral.satelliteRequest(SatelliteMethod.GetChangingRateOfMessages)
            }
        }
    }

    //

    private fun defaultParams() {
        _deviceData.value = DeviceData()
        _ggaMessage.value = GgaData()
        _gstMessage.value = GstData()
    }
}