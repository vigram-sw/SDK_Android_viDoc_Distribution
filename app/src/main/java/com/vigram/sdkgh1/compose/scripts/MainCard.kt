package com.vigram.sdkgh1.compose.scripts

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.vigram.sdkgh1.MainViewModel
import com.vigram.sdkgh1.ui.scripts.MainDeviceInfoCard
import com.vigram.sdkgh1.ui.scripts.NmeaCard
import com.vigram.sdkgh1.ui.scripts.NtripInfoCard
import com.vigram.sdkgh1.domain.entity.MainDeviceMethod
import com.vigram.sdk.Models.Satellite.SatelliteMethod
import com.vigram.sdkgh1.compose.event.LaserUiEvent

@Composable
fun MainInfoCard(viewModel: MainViewModel) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            // .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 6.dp, end = 6.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    },
                    onPress = {
                        focusManager.clearFocus()
                    })
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedButton(onClick = {
            viewModel.disconnectDevice()
        }) {
            Text("Disconnect")
        }

        MainDeviceInfoCard(viewModel = viewModel)

        ElevatedButton(onClick = {
            viewModel.deviceRequest(MainDeviceMethod.GetBattery)
        }) {
            Text("Get Battery Charger")
        }

        ElevatedButton(onClick = {
            viewModel.deviceRequest(MainDeviceMethod.GetVersionNumbers)
        }) {
            Text("Get Soft/Hard Version")
        }

        NmeaCard(
            gga = viewModel.ggaMessage.collectAsState(),
            gst = viewModel.gstMessage.collectAsState(),
            txt = viewModel.txtMessage.collectAsState()
        )

        LaserCard(laserUiEvent = viewModel::laserEvent)

        NtripInfoCard(viewModel = viewModel)

        SatelliteCard(
            dynamicStateListener = viewModel,
            onClickDopListener = {
                viewModel.satelliteRequest(method = SatelliteMethod.ChangeStatusNavDOP(it, false))
            },
            onClickPvtListener = {
                viewModel.satelliteRequest(method = SatelliteMethod.ChangeStatusNavPVT(it, false))
            },
            viewModel
        )
    }
}