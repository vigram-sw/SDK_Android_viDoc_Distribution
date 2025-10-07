package com.vigram.sdkgh1

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_ADMIN
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.vigram.sdk.Laser.LaserConfiguration
import com.vigram.sdk.Models.RequestResult
import com.vigram.sdkgh1.ui.scripts.FilePickerButton
import com.vigram.sdkgh1.ui.scripts.SearchDevicesCard
import com.vigram.sdkgh1.ui.scripts.SoftwareDialogCard
import com.vigram.sdkgh1.compose.scripts.AuthenticationCard
import com.vigram.sdkgh1.compose.scripts.ConfigurationCard
import com.vigram.sdkgh1.compose.scripts.MainInfoCard
import com.vigram.sdkgh1.compose.event.PeripheralEvent
import com.vigram.sdkgh1.compose.event.ScreenStatus
import com.vigram.sdkgh1.data.mapper.toLaserPositionSDK
import com.vigram.sdkgh1.domain.entity.MainDeviceMethod
import com.vigram.sdkgh1.domain.entity.MainLaserConfiguration
import com.vigram.sdkgh1.ui.theme.BlueTheme
import com.vigram.sdkgh1.ui.theme.GrayButton

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application)
    }

    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isLocationGranted =
            permissions.getOrDefault(ACCESS_FINE_LOCATION, false)
        val isBluetoothGranted = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> permissions.getOrDefault(
                BLUETOOTH_SCAN,
                false
            )

            else -> permissions.getOrDefault(
                BLUETOOTH_ADMIN,
                false
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isLocationGranted && isBluetoothGranted) {
                isBluetoothPermission.value = true
                onBluetoothPermissionGranted()
            } else {
                isBluetoothPermission.value = false
            }
        } else {
            if (isBluetoothGranted) {
                isBluetoothPermission.value = true
                onBluetoothPermissionGranted()
            } else {
                isBluetoothPermission.value = false
            }
        }
    }


    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBluetoothPermissionLauncher.launch(
                arrayOf(
                    BLUETOOTH_SCAN,
                    BLUETOOTH_CONNECT,
                    ACCESS_FINE_LOCATION
                )
            )
        } else {
            requestBluetoothPermissionLauncher.launch(
                arrayOf(
                    BLUETOOTH_ADMIN,
                    BLUETOOTH
                )
            )
        }
    }

    private var isBluetoothPermission = mutableStateOf(false)

    private fun onBluetoothPermissionGranted() {
        viewModel.authentication("")
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (savedInstanceState == null) {
            requestBluetoothPermission()
        }

        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()
            val primary = MaterialTheme.colorScheme.primary

            SideEffect {
                systemUiController.setStatusBarColor(
                    color = primary,
                    darkIcons = true
                )
            }

            BlueTheme(darkTheme = false) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    val stateScreen = viewModel.screenStatus.collectAsState()

                    when (val state = stateScreen.value) {
                        is ScreenStatus.Authentication -> {
                            AuthenticationCard(state.status)
                        }

                        is ScreenStatus.Configuration -> {
                            MainInfoCard(viewModel)
                            ConfigurationCard(
                                state.configurationStatus, viewModel::dialogConfigurationRead
                            )
                        }

                        is ScreenStatus.Peripheral -> {
                            when (state.status) {
                                PeripheralEvent.Connected -> MainInfoCard(viewModel)
                                PeripheralEvent.Disconnected -> {
                                }
                            }
                        }

                        is ScreenStatus.ScanDevices -> SearchDevice(state)
                        is ScreenStatus.SoftwareUpdate -> {
                            MainInfoCard(viewModel)
                            SoftwareUpdate(state)
                        }
                    }
                }
            }
        }
    }

    @Composable
    @SuppressLint("MissingPermission")
    private fun SearchDevice(state: ScreenStatus.ScanDevices) {
        FilePickerButton(
            softwareFile = viewModel.softwareFile.collectAsState(),
            onFileListener = viewModel::softwareFile
        )

        SearchDevicesCard(
            state.scanDevicesStatus,
            onStopClickListener = viewModel::stopScan,
            onStartClickListener = viewModel::startScan,
            onConnectClickListener = { viewModel.connectDevice(it) },
        )
    }

    @Composable
    private fun SoftwareUpdate(state: ScreenStatus.SoftwareUpdate) {
        SoftwareDialogCard(state.softwareUpdateStatus)
    }
}