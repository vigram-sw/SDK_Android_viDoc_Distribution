package com.vigram.sdkgh1.ui.scripts

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vigram.sdkgh1.compose.event.ScanDevicesEvent

@Composable
fun SearchDevicesCard(
    status: ScanDevicesEvent,
    onStopClickListener: () -> Unit,
    onStartClickListener: () -> Unit,
    onConnectClickListener: (BluetoothDevice) -> Unit
) {
    when (status) {
        is ScanDevicesEvent.Scan -> {
            ElevatedButton(onClick = onStopClickListener) { Text("Stop scan") }

            ListDevises(
                devices = status.devices.collectAsState(),
                onConnectListener = onConnectClickListener
            )
        }

        ScanDevicesEvent.Stop ->
            ElevatedButton(onClick = onStartClickListener) { Text("Start scan") }
    }


}

@Composable
fun ListDevises(
    modifier: Modifier = Modifier,
    devices: State<List<BluetoothDevice>>,
    onConnectListener: (BluetoothDevice) -> Unit
) {
    Box(modifier = modifier) {
        if (devices.value.isEmpty())
            CircularProgressIndicator(
                modifier = Modifier.width(32.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        else
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (true) 14.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(devices.value) { index, device ->
                    ElevatedButton(onClick = {
                        onConnectListener(device)
                    }) {
                        Text(device.name)
                    }
                }
            }
    }
}