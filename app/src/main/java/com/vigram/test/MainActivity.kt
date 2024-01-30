package com.vigram.test

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.LinearLayoutManager
import com.vigram.sdk.Bluetooth.BluetoothService
import com.vigram.sdk.GPS.GPSService
import com.vigram.sdk.General.Configuration
import com.vigram.sdk.Laser.LaserConfiguration
import com.vigram.sdk.Laser.LaserService
import com.vigram.sdk.Models.*
import com.vigram.sdk.Models.Satellite.SatelliteMessage
import com.vigram.sdk.NTRIP.NtripConnectionInformation
import com.vigram.sdk.NTRIP.NtripService
import com.vigram.sdk.NTRIP.NtripTask
import com.vigram.sdk.Peripheral.Peripheral
import com.vigram.sdk.Peripheral.PeripheralConfiguration
import com.vigram.sdk.SatelliteCommands.DynamicStateType
import com.vigram.sdk.SatelliteCommands.Elevation
import com.vigram.sdk.SatelliteCommands.NavigationSystemType
import com.vigram.sdk.SatelliteCommands.RateValue
import com.vigram.sdk.SinglePoint.SinglePointRecordModel
import com.vigram.sdk.Version.SoftwareService
import com.vigram.sdk.Vigram
import com.vigram.sdk.Vigram.Companion.gpsService
import com.vigram.sdk.Vigram.Companion.peripheralLogger
import com.vigram.test.adapters.RecyclerViewFilesAdapter
import com.vigram.test.adapters.RecyclerViewMountsAdapter
import com.vigram.test.adapters.RecyclerViewVersionsAdapter
import com.vigram.test.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

private const val TAG = "HOST"
private const val TOKEN = "your_token"

class MainActivity : AppCompatActivity() {
    private var context: Context? = null
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var binding: ActivityMainBinding
    private var bluetoothService: BluetoothService? = null
    private var peripheral: Peripheral? = null
    private var laserService: LaserService? = null
    private var ntripService: NtripService? = null
    private var ntripTask: NtripTask? = null
    private var ntripConnectionInformation: NtripConnectionInformation? = null

    private var gpsService: GPSService? = null
    private val bluetoothDevices = ArrayList<BluetoothDevice>()

    var currentMount: String? = null

    private val dispatcher = Dispatchers.Main
    val coroutineScope = CoroutineScope(dispatcher)

    private var host: String? = null
    private var port: Int? = null
    private var userName: String? = null
    private var password: String? = null

    private var versionsList: List<DeviceMessage.Software>? = null

    private var recyclerViewVersionAdapter: RecyclerViewVersionsAdapter? = null
    private var recyclerViewMountsAdapter: RecyclerViewMountsAdapter? = null
    private var recyclerViewUbxFilesAdapter: RecyclerViewFilesAdapter? = null
    private var recyclerViewLoggingFilesAdapter: RecyclerViewFilesAdapter? = null

    private var ggaMessage: GGAMessage? = null

    @SuppressLint("MissingPermission", "NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        context = this
        sharedPreferences = getSharedPreferences("PRIVATE", MODE_PRIVATE)

        initViews(binding)
        initGeneralSettingsSdk(binding)
        initGpsControl()
        initSatellitesControlButtons(binding)
        initElevationControlButtons(binding)
        initSetChangingRateControlButtons(binding)
        initRxmControlButton(binding)
        initLaserControlButtons(binding)
        initButtonLogger(binding)
        initButtonChangeDynamicState(binding)
        initButtonTaskListeners(binding)
        initButtonsListenersUbxControl(binding)
        initButtonsGettingDeviceInformation(binding)
        initSinglePointRecording(binding)
    }

    private fun initGpsControl() {
        binding.apply {
            buttonStartGpsService.setOnClickListener {
                startGpsService()
            }
            buttonStopGpsService.setOnClickListener {
                gpsService?.reconnect()
            }
        }
    }

    private fun initLaserControlButtons(binding: ActivityMainBinding) {
        binding.apply {
            buttonTurnOnBottomLaser.setOnClickListener {
                laserService?.turnLaserOn(LaserConfiguration.Position.bottom)
            }
            buttonTurnOnFrontLaser.setOnClickListener {
                laserService?.turnLaserOn(LaserConfiguration.Position.back)
            }
            buttonTurnOffLaser.setOnClickListener {
                laserService?.turnLaserOff(LaserConfiguration.Position.back)
            }

            buttonStartLaserDistanceRecordingByFront.setOnClickListener {
                val duration = try {
                    editTextDurationSimpleRecording.text.toString().toInt()
                } catch (e: java.lang.Exception) {
                    10
                }
                val laserConfiguration = LaserConfiguration(
                    LaserConfiguration.ShotMode.fast, LaserConfiguration.Position.back,
                    duration
                )
                try {
                    laserService?.record(laserConfiguration) { measurement ->
                        textViewStartValue.text =
                            "${getString(R.string.start)} ${measurement.start}"
                        textViewEndValue.text = "${getString(R.string.end)} ${measurement.end}"
                        textViewDistanceValue.text =
                            "${getString(R.string.distance)} ${measurement.distance}"
                        textViewQualityValue.text =
                            "${getString(R.string.quality)} ${measurement.quality}"

                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Laser recording. Exception: ${e.message}")
                }
            }

            buttonStartLaserDistanceRecordingByBottom.setOnClickListener {
                var duration = try {
                    editTextDurationSimpleRecording.text.toString().toInt()
                } catch (e: java.lang.Exception) {
                    10
                }
                val laserConfiguration = LaserConfiguration(
                    LaserConfiguration.ShotMode.fast, LaserConfiguration.Position.bottom,
                    duration
                )

                try {
                    laserService?.record(laserConfiguration) { measurement ->
                        textViewStartValue.text =
                            "${getString(R.string.start)} ${measurement.start}"
                        textViewEndValue.text = "${getString(R.string.end)} ${measurement.end}"
                        textViewDistanceValue.text =
                            "${getString(R.string.distance)} ${measurement.distance}"
                        textViewQualityValue.text =
                            "${getString(R.string.quality)} ${measurement.quality}"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Laser recording. Exception: ${e.message}")
                }
            }
        }
    }

    private fun initGeneralSettingsSdk(binding: ActivityMainBinding) {
        Configuration.debug = true
        Vigram.init(this, TOKEN)
            .check { authenticationData ->
                if (authenticationData.success) {
                    bluetoothService = Vigram.bluetoothService(this, this)
                    bluetoothService?.startScan()

                    bluetoothService!!.duration = 15
                    bluetoothService!!.getDevices { devices ->
                        bluetoothDevices.clear()
                        bluetoothDevices.addAll(devices)
                        adapterDevices.notifyDataSetChanged()
                    }

                    ntripService = Vigram.ntripService()

                } else {
                    Log.e(TAG, "Authentication data: ${authenticationData.message}")
                }
                binding.textViewToken.text =
                    "${getString(R.string.is_token_valid)} ${authenticationData.authEnum}"
            }


        //Creates a ``BluetoothService`` to scan for and connect to available peripherals.

        binding.apply {
            buttonDisconnect.setOnClickListener {
                peripheral?.disconnect()
            }

            buttonGetVersion.setOnClickListener {
                val softwareService = SoftwareService()
                softwareService.getAllAvailableSoftware { versionsList ->
                    this@MainActivity.versionsList = versionsList
                    recyclerViewVersionAdapter?.setData(versionsList)
                    recyclerViewVersionAdapter?.notifyDataSetChanged()
                }
            }

            buttonGetMounts.setOnClickListener {
                editTextHost.text?.toString()?.let { host ->
                    editTextPort.text?.toString()?.let { port ->
                        editTextUsername.text?.toString()?.let { username ->
                            editTextPassword.text?.toString()?.let { password ->
                                if (port.isDigitsOnly()) {
                                    ntripConnectionInformation = NtripConnectionInformation(
                                        host,
                                        port.toInt(),
                                        username,
                                        password
                                    )
                                    ntripConnectionInformation?.let {
                                        ntripService?.mountpoints(it) { points, message ->
                                            points?.forEach {
                                                Log.d(TAG, "Mounts: ${it.name}")
                                            }
                                            points?.let { p ->
                                                recyclerViewMountsAdapter?.setData(p)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initViews(binding: ActivityMainBinding) {
        recyclerViewMountsAdapter =
            RecyclerViewMountsAdapter(this@MainActivity, listOf(), binding, this@MainActivity)
        recyclerViewUbxFilesAdapter = RecyclerViewFilesAdapter(this@MainActivity, listOf(), binding)
        recyclerViewLoggingFilesAdapter =
            RecyclerViewFilesAdapter(this@MainActivity, listOf(), binding)
        recyclerViewVersionAdapter = RecyclerViewVersionsAdapter(this@MainActivity, listOf())
        binding.apply {
            devicesGrid.adapter = adapterDevices
            recyclerViewAvailableVersions.layoutManager = LinearLayoutManager(context)
            recyclerViewAvailableVersions.adapter = recyclerViewVersionAdapter
            recyclerViewMounts.layoutManager = LinearLayoutManager(context)
            recyclerViewMounts.adapter = recyclerViewMountsAdapter
            recyclerViewUbxFiles.layoutManager = LinearLayoutManager(context)
            recyclerViewUbxFiles.adapter = recyclerViewUbxFilesAdapter
            recyclerViewLoggingFiles.layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerViewLoggingFiles.adapter = recyclerViewLoggingFilesAdapter
        }
    }

    private fun initSinglePointRecording(binding: ActivityMainBinding) {
        binding.apply {
            buttonBottomSinglePointRecording.setOnClickListener {
                var duration = 10
                if (binding.editTextDurationOfLaser.text.toString()
                        .isDigitsOnly() && binding.editTextDurationOfLaser.text.isNotEmpty()
                ) {
                    duration = binding.editTextDurationOfLaser.text.toString().toInt()
                }

                val conf = LaserConfiguration(
                    LaserConfiguration.ShotMode.fast,
                    LaserConfiguration.Position.bottom,
                    duration
                )
                gpsService?.let { gpsService ->
                    laserService?.let { laserService ->
                        val singlePointRecordingService = Vigram.singlePointRecordingService(
                            gpsService = gpsService,
                            laserService = laserService,
                            laserConfiguration = conf,
                            dynamicStateType = DynamicStateType.STATIONARY
                        )
                        singlePointRecordingService.record(conf.duration, { callSingle ->
                            when (callSingle) {
                                is SinglePointRecordModel.Success -> {
                                    pasteSingleRecordingInfo(callSingle.data)
                                }
                                is SinglePointRecordModel.Error -> {
                                    pasteSingleRecordingInfo(callSingle.data)
                                }
                            }
                        }) { callAverage: SinglePointRecordModel<SinglePoint?>? ->
                            when (callAverage) {
                                is SinglePointRecordModel.Success -> {
                                    callAverage.data?.let {
                                        pasteAverageRecordingInfo(it)
                                    }
                                }
                                is SinglePointRecordModel.Error -> {
                                    callAverage.data?.let {
                                        pasteAverageRecordingInfo(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            buttonFrontSinglePointRecording.setOnClickListener {
                var duration = 10
                if (binding.editTextDurationOfLaser.text.toString()
                        .isDigitsOnly() && binding.editTextDurationOfLaser.text.isNotEmpty()
                ) {
                    duration = binding.editTextDurationOfLaser.text.toString().toInt()
                }
                val conf = LaserConfiguration(
                    LaserConfiguration.ShotMode.fast,
                    LaserConfiguration.Position.back,
                    duration
                )
                gpsService?.let { gpsService ->
                    laserService?.let { laserService ->
                        val singlePointRecordingService = Vigram.singlePointRecordingService(
                            gpsService = gpsService,
                            laserService = laserService,
                            laserConfiguration = conf,
                            dynamicStateType = DynamicStateType.STATIONARY
                        )
                        singlePointRecordingService.record(conf.duration, { callSingle ->
                            when (callSingle) {
                                is SinglePointRecordModel.Success -> {
                                    pasteSingleRecordingInfo(callSingle.data)
                                }
                                is SinglePointRecordModel.Error -> {
                                    pasteSingleRecordingInfo(callSingle.data)
                                }
                            }
                        }) { callAverage: SinglePointRecordModel<SinglePoint?>? ->
                            when (callAverage) {
                                is SinglePointRecordModel.Success -> {
                                    callAverage.data?.let {
                                        pasteAverageRecordingInfo(it)
                                    }
                                }
                                is SinglePointRecordModel.Error -> {
                                    callAverage.data?.let {
                                        pasteAverageRecordingInfo(it)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initButtonsListenersUbxControl(binding: ActivityMainBinding) {
        binding.apply {
            buttonCheckToken.setOnClickListener {
                textViewToken.text =
                    "${getString(R.string.is_token_valid)} ${Vigram.tokenIsValid().success}"
            }
            buttonDisableNAVPVTRAMOnly.setOnClickListener {
                textViewNAVPVTValue.visibility = View.GONE
                peripheral?.changeStatusNAVPVT(activate = false) {
                    Log.d(TAG, "changeStatusNAVPVT - ACK: ${it.getResult()}")
                }
            }
            buttonEnableNAVPVTRAMOnly.setOnClickListener {
                textViewNAVPVTValue.visibility = View.VISIBLE
                peripheral?.changeStatusNAVPVT(activate = true) {
                    Log.d(TAG, "changeStatusNAVPVT - ACK: ${it.getResult()}")
                }
            }
            buttonDisableNAVDOPRAMOnly.setOnClickListener {
                textViewNAVDOPValue.visibility = View.GONE
                peripheral?.changeStatusNAVDOP(activate = false) {
                    Log.d(TAG, "changeStatusNAVPVT - ACK: ${it.getResult()}")
                }
            }
            buttonEnableNAVDOPRAMOnly.setOnClickListener {
                textViewNAVDOPValue.visibility = View.VISIBLE
                peripheral?.changeStatusNAVDOP(activate = true) {
                    Log.d(TAG, "changeStatusNAVPVT - ACK: ${it.getResult()}")
                }
            }
        }
    }

    private fun initButtonTaskListeners(binding: ActivityMainBinding) {
        binding.buttonGetTask.setOnClickListener {
            ntripTask = currentMount?.let { mount ->
                ggaMessage?.let { gga ->
                    ntripConnectionInformation?.let { info ->
                        ntripService?.task(info, mount, gga)
                    }
                }
            }
            ntripTask?.start()
            ntripTask?.ntripState {
                binding.textViewStatusTask.text = "${getString(R.string.status_ntrip_task)} $it"
                Log.d(TAG, "NTRIP State: $it")
                if (it == NtripState.ERROR) {
                    Log.e(TAG, "NTRIP State: Error. Message: ${it.value}")
                }
                if (it == NtripState.CONNECT) {
                    ntripTask?.data {
                        Log.d(TAG, "bytes received by the user: ${it.toHex()}")
                    }
                }
            }
        }
        binding.buttonCancelTask.setOnClickListener {
            ntripTask?.cancel()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSatelliteMessages(message: SatelliteMessage) {
        if (message.dop != null) {
            Log.d(TAG, "NAV-DOP: ${message.dop}")
            binding.textViewNAVDOPValue.text = "${getString(R.string.navdop)} ${message.dop}"
        }
        if (message.pvt != null) {
            Log.d(TAG, "NAV-PVT: ${message.pvt}")
            binding.textViewNAVPVTValue.text = "${getString(R.string.navpvt)} ${message.pvt}"
        }
        if (message.navigationSystemStatus != null
            && message.navigationSystemStatus?.satelliteType != null
            && message.navigationSystemStatus?.isEnabled != null
        ) {
            binding.textViewSatellite.text =
                "${getString(R.string.satellite)} ${message.navigationSystemStatus!!.satelliteType!!.name}"
            binding.textViewSatelliteStatus.text =
                "${getString(R.string.satellite_status)} ${if (message.navigationSystemStatus!!.isEnabled!!) "Enabled" else "Disabled"}"
        }

        message.elevation?.let {
            Log.d(TAG, "Elevation: : ${it.angle}")
            binding.textViewCurrentMinimumElevation.text =
                "${getString(R.string.current_minimum_elevation)} ${it.angle}°"
        }

        message.rawx?.let {
            Log.d(TAG, "RAWX: ${message.sfrbx?.message}")
            binding.textViewRAWX.text = "${getString(R.string.rawx)} ${it.message}"
        }

        message.sfrbx?.let {
            Log.d(TAG, "SFRBX: ${message.sfrbx?.message}")
            binding.textViewSFRBX.text =
                "${getString(R.string.sfrbx)} ${it.message}"
        }

        message.changingRate?.let {
            Log.d(TAG, "Rate value: ${message.changingRate?.name}")
            binding.textViewCurrentRate.text =
                "${getString(R.string.get_current_rate)}: ${message.changingRate?.name}"
        }

        if (message.acknowledgedMessage != null) {
            Log.d(TAG, "ACK Message: ${message.acknowledgedMessage}")
        }
    }

    private var adapterDevices: BaseAdapter = object : BaseAdapter() {
        override fun getCount(): Int {
            return bluetoothDevices.size
        }

        override fun getItem(position: Int): Any {
            return bluetoothDevices[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        @SuppressLint("MissingPermission", "ViewHolder", "InflateParams", "SetTextI18n")
        override fun getView(position: Int, view: View?, parent: ViewGroup): View? {
            val adapterView = layoutInflater.inflate(R.layout.adapter, null)
            val textDevices: TextView = adapterView.findViewById(R.id.textView)
            textDevices.text = bluetoothDevices[position].name
            adapterView.setOnClickListener {
                binding.linearLayoutDeviceInfo.visibility = View.VISIBLE
                binding.textViewDeviceName.text = bluetoothDevices[position].name
                //Creates a Peripheral to control what is sent and received from the viDoc.
                bluetoothService?.stopScan()

                val peripheralConfiguration = PeripheralConfiguration(
                    rateOfChangeMessages = RateValue.HZ_01,
                    navDOPActivate = true,
                    navPVTActivate = true,
                )
                val bluetoothDevice = bluetoothDevices[position]

                val pathLog =
                    context?.filesDir?.path + "/" +
                            SimpleDateFormat("dd.MM.yyyy-hh-mm-ss").format(Date()) + ".txt"
                val fileLog = File(pathLog)
                val uri = Uri.fromFile(fileLog)
                val logger = peripheralLogger(uri)

                peripheral = Vigram.peripheral(
                    peripheral = bluetoothDevice,
                    peripheralConfiguration = peripheralConfiguration,
                    peripheralLogger = logger
                )

                peripheral?.configurationState {
                    binding.textViewConfigurationState.text = it.name
                }

                context?.let { it1 -> peripheral?.start(it1) }

                // Creates a ``LaserService`` to retrieve laser distance data from a viDoc device.
                laserService = peripheral?.let { it1 -> Vigram.laserService(it1) }
                peripheral?.state { state: PeripheralState ->
                    binding.textViewDeviceStatusConnection.text = "${state.name}"
                }

                peripheral?.deviceMessages { message ->
                    if (message.battery?.percentage != null) {
                        binding.textViewBattery.text = "${message.battery?.percentage}%"
                    }
                    if (message.version?.software != null) {
                        binding.textViewCurrentSoftwareVersion.text =
                            message.version?.software.toString()
                    }
                    if (message.version?.hardware != null) binding.textViewHardwareVersion.text =
                        "${message.version?.hardware?.major}.${message.version?.hardware?.minor}"
                }

                peripheral?.let { recyclerViewVersionAdapter?.setPeripheral(it) }

                peripheral?.nmea { nmeaMessage ->
                    binding.apply {
                        nmeaMessage.accuracy
                        if (nmeaMessage.getGGA() != null) {
                            Log.d(TAG, "GGA: ${nmeaMessage.getGGA()}")
                            textViewGga.text = nmeaMessage.getGGA()?.raw
                            ggaMessage = nmeaMessage.getGGA()
                        }

                        if (nmeaMessage.getGST() != null) {
                            Log.d(TAG, "GST: ${nmeaMessage.getGST()}")
                            textViewGst.text = nmeaMessage.getGST()?.raw
                        }
                    }
                }

                peripheral?.satelliteMessages {
                    coroutineScope.launch(Dispatchers.Main) {
                        updateSatelliteMessages(it)
                    }
                }

                peripheral?.softwareNotification { actual, current, callAnswer ->

                    binding.textViewActualSoftwareVersion.apply {
                        text = "$actual"
                    }

                    binding.buttonUpdateToActual.setOnClickListener {
                        callAnswer.invoke(true)
                    }
                }

                peripheral?.progressUpdateSoftware({ state, message ->

                    coroutineScope.launch(Dispatchers.Main) {
                        binding.textViewUpdateStatus.visibility = View.VISIBLE
                        when (state) {
                            StateUpdateSoftware.START_UPDATE -> {
                                binding.textViewUpdateStatus.text =
                                    "Update status: installing..."
                            }
                            StateUpdateSoftware.END_UPDATE -> {
                                context?.let { it1 ->
                                    showAlert(
                                        it1,
                                        "Update Information",
                                        "Firmware installation is successful"
                                    )
                                }
                                binding.textViewUpdateStatus.text =
                                    "Update status: installation completed successfully"
                            }
                            StateUpdateSoftware.ERROR_UPDATE -> {
                                context?.let { it1 ->
                                    showAlert(
                                        it1,
                                        "Update Information",
                                        "Firmware update error"
                                    )
                                }
                                binding.textViewUpdateStatus.text = "Update status: error"
                            }
                        }
                    }
                }, { progress ->
                    coroutineScope.launch(Dispatchers.Main) {
                        binding.textViewUpdateProgress.visibility = View.VISIBLE
                        binding.textViewUpdateProgress.text =
                            "Update progress: ${(progress * 10.0).roundToInt() / 10.0}%"
                    }
                })

            }
            return adapterView
        }
    }

    private fun initSatellitesControlButtons(binding: ActivityMainBinding) {
        binding.apply {
            // get status
            buttonStatusGlonass.setOnClickListener {
                peripheral?.getCurrentStatusGNSS(NavigationSystemType.GLONASS) {
                    Log.d(TAG, "${it.satelliteType} is enabled: ${it.isEnabled}")
                }
            }
            buttonStatusBeiDou.setOnClickListener {
                peripheral?.getCurrentStatusGNSS(NavigationSystemType.BEI_DOU) {
                    Log.d(TAG, "${it.satelliteType} is enabled: ${it.isEnabled}")
                }
            }
            buttonStatusGalileo.setOnClickListener {
                peripheral?.getCurrentStatusGNSS(NavigationSystemType.GALILEO) {
                    Log.d(TAG, "${it.satelliteType} is enabled: ${it.isEnabled}")
                }
            }
            buttonStatusQZSS.setOnClickListener {
                peripheral?.getCurrentStatusGNSS(NavigationSystemType.QZSS) {
                    Log.d(TAG, "${it.satelliteType} is enabled: ${it.isEnabled}")
                }
            }
            buttonStatusSBAS.setOnClickListener {
                peripheral?.getCurrentStatusGNSS(NavigationSystemType.SBAS) {
                    Log.d(TAG, "${it.satelliteType} is enabled: ${it.isEnabled}")
                }
            }

            // enable
            buttonEnableGlonass.setOnClickListener {
                peripheral?.changeStatusGNSS(NavigationSystemType.GLONASS, true) {
                    Log.d(TAG, "changeStatusGNSS - ACK: ${it.getResult()}")
                }
            }
            buttonEnableBeiDou.setOnClickListener {
                peripheral?.changeStatusGNSS(NavigationSystemType.BEI_DOU, true) {
                    Log.d(TAG, "changeStatusGNSS - ACK: ${it.getResult()}")
                }
            }
            buttonEnableGalileo.setOnClickListener {
                peripheral?.changeStatusGNSS(NavigationSystemType.GALILEO, true) {
                    Log.d(TAG, "changeStatusGNSS - ACK: ${it.getResult()}")
                }
            }
            buttonEnableQZSS.setOnClickListener {
                peripheral?.changeStatusGNSS(NavigationSystemType.QZSS, true) {
                    Log.d(TAG, "changeStatusGNSS - ACK: ${it.getResult()}")
                }
            }
            buttonEnableSBAS.setOnClickListener {
                peripheral?.changeStatusGNSS(NavigationSystemType.SBAS, true) {
                    Log.d(TAG, "changeStatusGNSS - ACK: ${it.getResult()}")
                }
            }

            // disable
            buttonDisableGlonass.setOnClickListener {
                peripheral?.changeStatusGNSS(NavigationSystemType.GLONASS, false) {
                    Log.d(TAG, "changeStatusGNSS - ACK: ${it.getResult()}")
                }
                peripheral?.changeStatusGNSS(NavigationSystemType.BEI_DOU, false) {
                    Log.d(TAG, "changeStatusGNSS - ACK: ${it.getResult()}")
                }
            }
            buttonDisableBeiDou.setOnClickListener {
                peripheral?.changeStatusGNSS(NavigationSystemType.BEI_DOU, false) {
                    Log.d(TAG, "changeStatusGNSS - ACK: ${it.getResult()}")
                }
            }
            buttonDisableGalileo.setOnClickListener {
                peripheral?.changeStatusGNSS(NavigationSystemType.GALILEO, false) {
                    Log.d(TAG, "changeStatusGNSS - ACK: ${it.getResult()}")
                }
            }
            buttonDisableQZSS.setOnClickListener {
                peripheral?.changeStatusGNSS(NavigationSystemType.QZSS, false) {
                    Log.d(TAG, "changeStatusGNSS - ACK: ${it.getResult()}")
                }
            }
            buttonDisableSBAS.setOnClickListener {
                peripheral?.changeStatusGNSS(NavigationSystemType.SBAS, false) {
                    Log.d(TAG, "changeStatusGNSS - ACK: ${it.getResult()}")
                }
            }

            // activate all
            buttonActivateAllGNSS.setOnClickListener {
                peripheral?.activateAllConstellationGNSS {
                    Log.d(TAG, "activateAllConstellationGNSS - ACK: ${it.getResult()}")
                }
            }
        }
    }

    private fun initElevationControlButtons(binding: ActivityMainBinding) {
        binding.apply {
            buttonGetCurrentMinimumElevation.setOnClickListener {
                peripheral?.getCurrentMinimumElevation {
                    Log.d(TAG, "Current angle: $it")
                }
            }

            buttonSetElevation00.setOnClickListener {
                peripheral?.setMinimumElevation(Elevation.ANGLE_0) {
                    Log.d(TAG, "setMinimumElevation - ACK: $it")
                }
            }

            buttonSetElevation05.setOnClickListener {
                peripheral?.setMinimumElevation(Elevation.ANGLE_5) {
                    Log.d(TAG, "setMinimumElevation - ACK: $it")
                }
            }

            buttonSetElevation10.setOnClickListener {
                peripheral?.setMinimumElevation(Elevation.ANGLE_10) {
                    Log.d(TAG, "setMinimumElevation - ACK: $it")
                }
            }

            buttonSetElevation15.setOnClickListener {
                peripheral?.setMinimumElevation(Elevation.ANGLE_15) {
                    Log.d(TAG, "setMinimumElevation - ACK: $it")
                }
            }

            buttonSetElevation20.setOnClickListener {
                peripheral?.setMinimumElevation(Elevation.ANGLE_20) {
                    Log.d(TAG, "setMinimumElevation - ACK: $it")
                }
            }

            buttonSetElevation25.setOnClickListener {
                peripheral?.setMinimumElevation(Elevation.ANGLE_25) {
                    Log.d(TAG, "setMinimumElevation - ACK: $it")
                }
            }

            buttonSetElevation30.setOnClickListener {
                peripheral?.setMinimumElevation(Elevation.ANGLE_30) {
                    Log.d(TAG, "setMinimumElevation - ACK: $it")
                }
            }

            buttonSetElevation35.setOnClickListener {
                peripheral?.setMinimumElevation(Elevation.ANGLE_35) {
                    Log.d(TAG, "setMinimumElevation - ACK: $it")
                }
            }

            buttonSetElevation40.setOnClickListener {
                peripheral?.setMinimumElevation(Elevation.ANGLE_40) {
                    Log.d(TAG, "setMinimumElevation - ACK: $it")
                }
            }
        }
    }

    private fun initSetChangingRateControlButtons(binding: ActivityMainBinding) {
        binding.apply {
            buttonSetRamHz1.setOnClickListener {
                peripheral?.setChangingRateOfMessages(RateValue.HZ_01) {
                    Log.d(TAG, "setChangingRateOfMessages - ACK: $it")
                }
            }
            buttonSetRamHz2.setOnClickListener {
                peripheral?.setChangingRateOfMessages(RateValue.HZ_02) {
                    Log.d(TAG, "setChangingRateOfMessages - ACK: $it")
                }
            }
            buttonSetRamHz3.setOnClickListener {
                peripheral?.setChangingRateOfMessages(RateValue.HZ_03) {
                    Log.d(TAG, "setChangingRateOfMessages - ACK: $it")
                }
            }
            buttonSetRamHz4.setOnClickListener {
                peripheral?.setChangingRateOfMessages(RateValue.HZ_04) {
                    Log.d(TAG, "setChangingRateOfMessages - ACK: $it")
                }
            }
            buttonSetRamHz5.setOnClickListener {
                peripheral?.setChangingRateOfMessages(RateValue.HZ_05) {
                    Log.d(TAG, "setChangingRateOfMessages - ACK: $it")
                }
            }
            buttonSetRamHz6.setOnClickListener {
                peripheral?.setChangingRateOfMessages(RateValue.HZ_06) {
                    Log.d(TAG, "setChangingRateOfMessages - ACK: $it")
                }
            }
            buttonSetRamHz7.setOnClickListener {
                peripheral?.setChangingRateOfMessages(RateValue.HZ_07) {
                    Log.d(TAG, "setChangingRateOfMessages - ACK: $it")
                }
            }
            buttonSetRamHz8.setOnClickListener {
                peripheral?.setChangingRateOfMessages(RateValue.HZ_08) {
                    Log.d(TAG, "setChangingRateOfMessages - ACK: $it")
                }
            }
            buttonSetRamHz9.setOnClickListener {
                peripheral?.setChangingRateOfMessages(RateValue.HZ_09) {
                    Log.d(TAG, "setChangingRateOfMessages - ACK: $it")
                }
            }
            buttonSetRamHz10.setOnClickListener {
                peripheral?.setChangingRateOfMessages(RateValue.HZ_10) {
                    Log.d(TAG, "setChangingRateOfMessages - ACK: $it")
                }
            }
            buttonGetCurrentRate.setOnClickListener {
                peripheral?.getCurrentRateOfMessages {
                    Log.d(TAG, "getCurrentRateOfMessages - ACK: $it")
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun pasteSingleRecordingInfo(info: SinglePoint) {
        binding.apply {
            textViewSinglePointDate.text =
                "${getString(R.string.date)} ${info.environmentData.date}"
            textViewSinglePointLatitude.text =
                "${getString(R.string.latitude)} ${info.environmentData.coordinate?.latitude.toString()}"
            textViewSinglePointLongitude.text =
                "${getString(R.string.longitude)} ${info.environmentData.coordinate?.longitude.toString()}"
            textViewSinglePointRefenceAltitude.text =
                "${getString(R.string.reference_altitude)} ${info.environmentData.coordinate?.referenceAltitude.toString()}"
            textViewSinglePointGeoidSeparation.text =
                "${getString(R.string.geoid_separation)} ${info.environmentData.coordinate?.geoidSeparation.toString()}"
            textViewSinglePointAltitude.text =
                "${getString(R.string.altitude)} ${info.environmentData.coordinate?.altitude.toString()}"
            textViewSinglePointCorrectionHeight.text =
                "${getString(R.string.correction_height)} ${info.environmentData.correctionHeight.toString()}"
            textViewSinglePointVerticalAccuracy.text =
                "${getString(R.string.vertical_accuracy)} ${info.environmentData.verticalAccuracy.toString()}"
            textViewSinglePointHorizontalAccuracy.text =
                "${getString(R.string.horizontal_accuracy)} ${info.environmentData.horizontalAccuracy.toString()}"
            textViewSinglePointDistance.text =
                "${getString(R.string.distance)} ${info.environmentData.distance.toString()}"
            textViewSinglePointDuration.text =
                "${getString(R.string.duration)} ${info.duration}"
            textViewSinglePointValid.text = "${getString(R.string.valid)} ${info.valid}"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun pasteAverageRecordingInfo(info: SinglePoint) {
        binding.apply {
            textViewAveragePointDate.text =
                "${getString(R.string.date)} ${info.environmentData.date}"
            textViewAveragePointLatitude.text =
                "${getString(R.string.latitude)} ${info.environmentData.coordinate?.latitude.toString()}"
            textViewAveragePointLongitude.text =
                "${getString(R.string.longitude)} ${info.environmentData.coordinate?.longitude.toString()}"
            textViewAveragePointRefenceAltitude.text =
                "${getString(R.string.reference_altitude)} ${info.environmentData.coordinate?.referenceAltitude.toString()}"
            textViewAveragePointGeoidSeparation.text =
                "${getString(R.string.geoid_separation)} ${info.environmentData.coordinate?.geoidSeparation.toString()}"
            textViewAveragePointAltitude.text =
                "${getString(R.string.altitude)} ${info.environmentData.coordinate?.altitude.toString()}"
            textViewAveragePointCorrectionHeight.text =
                "${getString(R.string.correction_height)} ${info.environmentData.correctionHeight.toString()}"
            textViewAveragePointVerticalAccuracy.text =
                "${getString(R.string.vertical_accuracy)} ${info.environmentData.verticalAccuracy.toString()}"
            textViewAveragePointHorizontalAccuracy.text =
                "${getString(R.string.horizontal_accuracy)} ${info.environmentData.horizontalAccuracy.toString()}"
            textViewAveragePointDistance.text =
                "${getString(R.string.distance)} ${info.environmentData.distance.toString()}"
            textViewAveragePointDuration.text =
                "${getString(R.string.duration)} ${info.duration}"
            textViewAveragePointValid.text = "${getString(R.string.valid)} ${info.valid}"
        }
    }

    private fun savePref(info: NtripConnectionInformation, mount: String) {
        val editor = sharedPreferences.edit()
        editor.apply {
            putString("HOST", info.hostname)
            putInt("PORT", info.port)
            putString("USER_NAME", info.username)
            putString("PASSWORD", info.password)
            putString("LAST_MOUNT", mount)
            apply()
        }
    }

    private fun getDataFromPref() {
        this.sharedPreferences.apply {
            host = getString("HOST", "")
            port = getInt("PORT", -1)
            userName = getString("USER_NAME", "")
            password = getString("PASSWORD", "")
        }
        host?.let { host ->
            port?.let { port ->
                userName?.let { username ->
                    password?.let { password ->
                        ntripConnectionInformation =
                            NtripConnectionInformation(host, port, username, password)
                        binding.apply {
                            editTextHost.setText(host)
                            editTextPort.setText(port.toString())
                            editTextUsername.setText(username)
                            editTextPassword.setText(password)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getLastMount() {
        currentMount = sharedPreferences.getString("LAST_MOUNT", "")
        binding.textViewMount.text = "${getString(R.string.mount)} $currentMount"
    }


    @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
    private fun initRxmControlButton(binding: ActivityMainBinding) {
        binding.apply {
            buttonEnableRXM.setOnClickListener {
                peripheral?.changeStatusRXM(activate = true)
                textViewIsRxmActive.text = "${getString(R.string.rxm_is)} active"
            }
            buttonDisableRXM.setOnClickListener {
                peripheral?.changeStatusRXM(activate = false)
                textViewIsRxmActive.text = "${getString(R.string.rxm_is)} inactivate"
            }
            buttonStartPpk.setOnClickListener {
                val path =
                    context?.filesDir?.path + "/" + SimpleDateFormat("dd.MM.yyyy-hh-mm-ss").format(
                        Date()
                    ) + ".ubx"
//                peripheral?.startRecordPPKMeasurements(path)

                val file = File(path)
                val uri = Uri.fromFile(file)
                peripheral?.startRecordPPKMeasurements(uri)

                textViewIsPpkRecordIsActive.text = "${getString(R.string.ppk_record_is)} active"
            }
            buttonStopPpk.setOnClickListener {
                peripheral?.stopRecordPPKMeasurements()
                textViewIsPpkRecordIsActive.text =
                    "${getString(R.string.ppk_record_is)} inactivate"

            }
            buttonGetAllRecordFiles.setOnClickListener {
                val fileList = fileList()
                recyclerViewUbxFilesAdapter?.setData(
                    fileList.toList().filter { it.contains("ubx") })
                recyclerViewUbxFilesAdapter?.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initButtonLogger(binding: ActivityMainBinding) {
        binding.buttonGetLogFiles.setOnClickListener {
            val fileList = fileList()
            recyclerViewLoggingFilesAdapter?.setData(
                fileList.toList().filter { it.contains("txt") })
            recyclerViewLoggingFilesAdapter?.notifyDataSetChanged()
        }
    }

    private fun initButtonChangeDynamicState(binding: ActivityMainBinding) {
        binding.apply {
            buttonSetPedestrianModeInRamOnly.setOnClickListener {
                peripheral?.setDynamicState(DynamicStateType.PEDESTRIAN) {
                    Log.d(TAG, "setDynamicState - ACK: $it")
                }
            }

            buttonSetStationaryModeInRamOnly.setOnClickListener {
                peripheral?.setDynamicState(DynamicStateType.STATIONARY) {
                    Log.d(TAG, "setDynamicState - ACK: $it")
                }
            }
            buttonGetCurrentDynamicState.setOnClickListener {
                peripheral?.getCurrentDynamicState {
                    textViewCurrentDynamicState.text =
                        "${getString(R.string.current_dynamic_state)} ${it.name}"
                }
            }
        }
    }

    private fun initButtonsGettingDeviceInformation(binding: ActivityMainBinding) {
        binding.apply {
            buttonGetCurrentSoftware.setOnClickListener {
                peripheral?.requestVersion {
                    textViewCurrentSoftwareVersion.text =
                        "${it.software.toString()}"
                }
            }
            buttonGetCurrentBattery.setOnClickListener {
                peripheral?.requestBattery {
                    textViewBattery.text =
                        "${it.percentage}%"
                }
            }

        }
    }


    companion object {
        fun showAlert(context: Context, title: String, message: String) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)

            builder.setNeutralButton("Ok") { _, _ ->
            }
            builder.show()
        }
    }

    private fun startGpsService() {
        if (ntripTask == null) {
            if (ntripConnectionInformation == null) getDataFromPref()
            if (currentMount == null) getLastMount()
            if (ntripConnectionInformation == null) {
                getDataFromPref()
            }
            if (currentMount == null) {
                getLastMount()
            }
            ntripTask = currentMount?.let { mp ->
                ntripConnectionInformation?.let { info ->
                    savePref(info, mp)
                    ggaMessage?.let { gga ->
                        ntripService?.task(
                            ntripInformation = info,
                            mountPoint = mp,
                            message = gga
                        )
                    }
                }
            }
        }
        ntripTask?.let { task ->
            peripheral?.let { peripheral ->
                gpsService = gpsService(peripheral, task)
                gpsService?.let {
                    it.apply {
                        start { ntripState ->
                            binding.textViewNtripState.apply {
                                text = "${getString(R.string.ntrip_state)} ${ntripState.name}"
                                Log.d(TAG, "NTRIP state: $ntripState")
                            }
                        }
                        coordinate { gpsCoordinate: GPSCoordinate ->
                            binding.apply {
                                textViewLatitude.text =
                                    "${getString(R.string.latitude)} ${gpsCoordinate.latitude.toString()}"
                                textViewLongitude.text =
                                    "${getString(R.string.longitude)} ${gpsCoordinate.longitude.toString()}"
                                textViewAltitude.text =
                                    "${getString(R.string.altitude)} ${gpsCoordinate.altitude.toString()}"
                                textViewRefenceAltitude.text =
                                    "${getString(R.string.reference_altitude)} ${gpsCoordinate.referenceAltitude.toString()}"
                            }
                        }
                        horizontalAccuracy { hacc ->
                            binding.textViewHorizontalAccuracy.apply {
                                text = "${getString(R.string.horizontal_accuracy)} $hacc"
                            }
                        }
                        verticalAccuracy { vacc ->
                            binding.textViewVerticalAccuracy.apply {
                                text = "${getString(R.string.vertical_accuracy)} $vacc"
                            }
                        }
                        quality { quality ->
                            binding.textViewQuality.apply {
                                text = "${getString(R.string.quality)} $quality"
                            }
                        }
                        hdop { hdop ->
                            binding.textViewGpsHdop.apply {
                                text = "${getString(R.string.hdop)} $hdop"
                            }
                        }
                        timestampedCoordinate { date, gpsCoordinate ->
                            binding.apply {
                                textViewTimestampedLatitude.text =
                                    "${getString(R.string.latitude)} ${gpsCoordinate.latitude}"
                                textViewTimestampedLongitude.text =
                                    "${getString(R.string.longitude)} ${gpsCoordinate.longitude}"
                                textViewTimestampedAltitude.text =
                                    "${getString(R.string.altitude)} ${gpsCoordinate.altitude}"
                                textViewTimestampedRefenceAltitude.text =
                                    "${getString(R.string.reference_altitude)} ${gpsCoordinate.referenceAltitude}"
                                textViewTimestampedDate.text =
                                    "${getString(R.string.date)} $date"
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ByteArray.toHex(): String {
        var hexString = ""
        this.toUByteArray().forEach { ubyte ->
            hexString += if (Integer.toHexString(ubyte.toInt()).length == 1) {
                "0" + Integer.toHexString(ubyte.toInt()) + " "
            } else {
                Integer.toHexString(ubyte.toInt()) + " "
            }
        }
        return hexString.uppercase()
    }
}