//package com.vigram.sdkgh1.compose.scripts
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.width
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.text.isDigitsOnly
//import com.vigram.sdk.Laser.LaserConfiguration
//
//
//@Composable
//private fun SinglePointMeasurementWidgets() {
//    var useLaser by remember { mutableStateOf(true) }
//    var measurementDuration by remember { mutableStateOf("5") }
//    Row(horizontalArrangement = Arrangement.SpaceBetween)
//    {
//        CheckBoxButton(150, txt = "Use laser", useLaser) {
//            useLaser = true
//        }
//
//        Spacer(Modifier.width(10.dp))
//
//        CheckBoxButton(150, txt = "Without laser", !useLaser) {
//            useLaser = false
//        }
//    }
//
//    Spacer(Modifier.height(5.dp))
//
//    TextFieldWithLabelDigitsOnly("Duration (second):", measurementDuration) { newText ->
//        if (newText.isDigitsOnly()) measurementDuration = newText
//    }
//
//    Spacer(Modifier.height(5.dp))
//
//    TextInfo(title = "The distance to the ground (cm)", info = distanceToGround.value)
//
//    Spacer(Modifier.height(5.dp))
//
//    ButtonWithBorder(txt = "Start SP measurement") {
//        if (measurementDuration.isDigitsOnly() && measurementDuration.isNotBlank()) {
//            ntripHelper?.let { ntrip ->
//                if (ntrip.isTaskInit)
//                    singlePointHelper = SinglePointHelper(
//                        this@MainActivity,
//                        bluetoothHelper.peripheral,
//                        ntrip.task
//                    )
//                else {
//                    this.ShowToast("Need connection to NTRIP")
//                    return@ButtonWithBorder
//                }
//            } ?: run {
//                this.ShowToast("Need connection to NTRIP")
//                return@ButtonWithBorder
//            }
//
//
//            val laserConfiguration = LaserConfiguration(
//                LaserConfiguration.ShotMode.Fast,
//                LaserConfiguration.Position.Front,
//                measurementDuration.toInt()
//            )
//
//            countDownSp.value = measurementDuration.toInt()
//
//            singlePointMeasurementState.value = SinglePointMeasurementState.RECORDING
//            singlePointHelper?.startMeasurement(laserConfiguration)
//
////                startTimer()
//        } else {
//            this.ShowToast("Enter the measurement duration")
//        }
//    }
//
//    when (singlePointMeasurementState.value) {
//        SinglePointMeasurementState.RECORDING -> {
//            Row(modifier = Modifier.fillMaxWidth()) {
//                TimeCount((countDownSp.value + 1).toString())
//            }
//        }
//
//        SinglePointMeasurementState.FINISHED -> {
//            SinglePointResult()
//        }
//
//        SinglePointMeasurementState.NOT_RECORDING -> {
//
//        }
//
//        SinglePointMeasurementState.ERROR -> {
//            AlertDialog(
//                onDismissRequest = {
//                    singlePointMeasurementState.value = SinglePointMeasurementState.NOT_RECORDING
//                },
//                title = {
//                    Text(
//                        text = "Error",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                },
//                text = {
//                    Text(
//                        text = "Bad measurement",
//                        fontSize = 14.sp
//                    )
//                },
//                confirmButton = {
//                    ButtonWithBorder(txt = "OK") {
//                        singlePointMeasurementState.value =
//                            SinglePointMeasurementState.NOT_RECORDING
//                    }
//                },
//            )
//        }
//    }
//}