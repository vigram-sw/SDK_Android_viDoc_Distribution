package com.vigram.sdkgh1.ui.scripts

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vigram.sdkgh1.R
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.style.TextAlign
import com.vigram.sdkgh1.MainViewModel

@Composable
fun MainDeviceInfoCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val deviceData = viewModel.deviceData.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                style = TextStyle(fontWeight = FontWeight.Bold),
                fontSize = 16.sp,
                text = "Device info"
            )
        }
        Info(title = "Version SDK", info = deviceData.value.sdkVersion)
        Spacer(modifier = Modifier.height(6.dp))
        InfoEditText(
            title = "BlePacketSize:",
            value = deviceData.value.blePacketSize,
            onTextChange = {
                viewModel.changeBlePacketSize(it)
            }
        )

        Spacer(modifier = Modifier.height(6.dp))
        InfoEditText(
            title = "BleWriteTimeOut:",
            value = deviceData.value.bleWriteTimeout.toString(),
            onTextChange = {
                viewModel.changeBleWriteTimeout(it)
            }
        )
        Spacer(modifier = Modifier.height(6.dp))
        InfoEditText(
            title = "BleSendInterval:",
            value = deviceData.value.bleSendInterval,
            onTextChange = {
                viewModel.changeBleSendInterval(it)
            }
        )
        Spacer(modifier = Modifier.height(6.dp))
        InfoEditText(
            title = "SoftwareSendDelay (ms):",
            value = deviceData.value.softwareSendDelay,
            onTextChange = {
                viewModel.changeSoftwareSendDelay(it)
            }
        )

        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Name Device", info = deviceData.value.nameDevice)
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Protocol", info = deviceData.value.protocolVersion)
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Serial number", info = deviceData.value.serialNumber)
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Device number", info = deviceData.value.deviceNumber)
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Has front laser", info = if (deviceData.value.hasFrontLaser) "+" else "-")
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Has bottom laser", if (deviceData.value.hasBottomLaser) "+" else "-")
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Has IMU", info = if (deviceData.value.hasIMU) "+" else "-")
        Spacer(modifier = Modifier.height(6.dp))
        Info(
            title = "Has calibrated",
            info = if (deviceData.value.hasCalibrated) "+" else "-"
        )
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Housing", info = deviceData.value.housing)
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Mount", info = deviceData.value.mount)
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "HW Ref.", info = deviceData.value.hwRef)
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "HW Bat.", info = deviceData.value.hwBat)
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Current device type", info = deviceData.value.currentDeviceType)
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Hardware on device", info = deviceData.value.hardware)
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Software on device", info = deviceData.value.software)
        Spacer(modifier = Modifier.height(6.dp))
        Info(title = "Battery", info = deviceData.value.battery)
        Spacer(modifier = Modifier.height(6.dp))
        ImageInfo(title = "Connection perephiral status", deviceData.value.connectionStatus)
    }
}

@Composable
private fun Info(
    title: String,
    info: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$title:", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = info)
    }
}

@Composable
private fun InfoEditText(title: String, value: String, onTextChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(4.dp))
        CustomTextField(value = value, onValueChange = onTextChange)
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
            .width(60.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        value = value,
        singleLine = true,
        onValueChange = onValueChange,
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                innerTextField()
            }
        }
    )
}


@Composable
private fun ImageInfo(title: String, iss: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        if (iss) {
            Image(
                modifier = Modifier.size(20.dp, 20.dp),
                painter = painterResource(id = R.drawable.ic_check_svg),
                contentDescription = "check"
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.Red
            )
        }
    }
}