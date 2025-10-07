package com.vigram.sdkgh1.compose.scripts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vigram.sdkgh1.MainViewModel
import com.vigram.sdkgh1.R
import com.vigram.sdkgh1.compose.event.ChangingRateUiEvent
import com.vigram.sdkgh1.compose.event.ElevationUiEvent
import com.vigram.sdkgh1.compose.event.GnssUiEvent
import com.vigram.sdk.Models.Satellite.DynamicStateType
import com.vigram.sdk.Models.Satellite.Elevation
import com.vigram.sdk.Models.Satellite.NavigationSystemType
import com.vigram.sdk.Models.Satellite.Rate
import kotlinx.coroutines.flow.StateFlow
import kotlin.String

interface DynamicStateListener {
    val state: StateFlow<String>
    fun onCurrentDynamicState()
    fun onSetDynamicState(dynamicStateValue: DynamicStateType)
}

@Composable
fun SatelliteCard(
    dynamicStateListener: DynamicStateListener,
    onClickDopListener: (Boolean) -> Unit,
    onClickPvtListener: (Boolean) -> Unit,
    viewModel: MainViewModel,
) {
    DynamicState(dynamicStateListener)
    NavPvtController(onClickDopListener, onClickPvtListener)
    GNSSControlPanel(viewModel)
    ElevetionControlPanel(viewModel)
    ChangingRateControlPanel(viewModel)
}


@Composable
private fun DynamicState(
    dynamicStateListener: DynamicStateListener,
) {
    val state by dynamicStateListener.state.collectAsState()

    var expanded by rememberSaveable { mutableStateOf(false) }
    val list = DynamicStateType.entries

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedButton(onClick = { expanded = !expanded }) {
            Row {
                Image(
                    painter = painterResource(if (expanded) R.drawable.expand_less else R.drawable.expand_more),
                    contentDescription = "Expand more"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (expanded) "Hide Dynamic state control" else "Show Dynamic state control")
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ElevatedButton(onClick = { dynamicStateListener.onCurrentDynamicState() }) {
                    Text("Current Dynamic state: $state")
                }

                list.forEach {
                    ElevatedButton(onClick = { dynamicStateListener.onSetDynamicState(it) }) {
                        Text("Set ${it.name}")
                    }
                }
            }
        }
    }
}

@Composable
private fun NavPvtController(
    onClickDopListener: (Boolean) -> Unit,
    onClickPvtListener: (Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ElevatedButton(onClick = { expanded = !expanded }) {
            Row {
                Image(
                    painter = painterResource(if (expanded) R.drawable.expand_less else R.drawable.expand_more),
                    contentDescription = "Expand more"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (expanded) "Hide NAV-DOP/PVT control" else "Show NAV-DOP/PVT control")
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("NavDop")
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    ElevatedButton(onClick = { onClickDopListener(true) }) {
                        Text("Enable")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    ElevatedButton(onClick = { onClickDopListener(false) }) {
                        Text("Disable")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("NavPvt")
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    ElevatedButton(onClick = { onClickPvtListener(true) }) {
                        Text("Enable")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    ElevatedButton(onClick = { onClickPvtListener(false) }) {
                        Text("Disable")
                    }
                }
            }
        }
    }
}

@Composable
private fun GNSSControlPanel(viewModel: MainViewModel) {

    var contentVisible by rememberSaveable { mutableStateOf(false) }
    val satelliteState by viewModel.stateSatellite.collectAsState(null)

    val status = when (satelliteState?.status) {
        true -> "is Enabled"
        false -> "is Disabled"
        else -> ""
    }

    val name = satelliteState?.value?.name ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedButton(onClick = { contentVisible = !contentVisible }) {
            Row {
                Image(
                    painter = painterResource(if (contentVisible) R.drawable.expand_less else R.drawable.expand_more),
                    contentDescription = "Expand more"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (contentVisible) "Hide Constellation control" else "Show Constellation control")
            }
        }

        AnimatedVisibility(contentVisible) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Satellite: $name")
                    Text("Status: $status")


                    ElevatedButton(onClick = {
                        viewModel.satelliteStatusEvent(
                            GnssUiEvent.GetSatelliteStatus(NavigationSystemType.Gps)
                        )
                    }) {
                        Text("Status GPS")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val constellations =
                    NavigationSystemType.entries.filter { it != NavigationSystemType.Gps }

                constellations.forEach { constellation ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ElevatedButton(
                            modifier = Modifier.width(150.dp),
                            onClick = {
                                viewModel.satelliteStatusEvent(
                                    GnssUiEvent.GetSatelliteStatus(constellation)
                                )
                            }
                        ) {
                            Text("Status ${constellation.name}")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        ElevatedButton(
                            onClick = {
                                viewModel.satelliteStatusEvent(
                                    GnssUiEvent.ChangeSatelliteStatus(constellation, true)
                                )
                            },
                        ) { Text("Enable") }
                        Spacer(modifier = Modifier.width(6.dp))
                        ElevatedButton(
                            onClick = {
                                viewModel.satelliteStatusEvent(
                                    GnssUiEvent.ChangeSatelliteStatus(constellation, false)
                                )
                            },
                        ) { Text("Disable") }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                ElevatedButton(onClick = { viewModel.satelliteStatusEvent(GnssUiEvent.ActivateAllSatellite) }) {
                    Text("Activate all constellation GNSS")
                }
            }
        }
    }
}

@Composable
private fun ElevetionControlPanel(viewModel: MainViewModel) {

    var contentVisible by rememberSaveable { mutableStateOf(false) }
    val list = Elevation.entries.chunked(3)
    val state by viewModel.stateElevation.collectAsState(null)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedButton(onClick = { contentVisible = !contentVisible }) {
            Row {
                Image(
                    painter = painterResource(if (contentVisible) R.drawable.expand_less else R.drawable.expand_more),
                    contentDescription = "Expand more"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (contentVisible) "Hide Elevation control" else "Show Elevation control")
            }
        }

        AnimatedVisibility(contentVisible) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Current elevation: ${state?.value?.value ?: ""}")
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedButton(onClick = {
                    viewModel.elevationControl(ElevationUiEvent.GetElevation)
                }) {
                    Text("Get elevation")
                }
                Spacer(modifier = Modifier.height(8.dp))

                list.forEach { items ->
                    Row {
                        items.forEach {
                            ElevatedButton(onClick = {
                                viewModel.elevationControl(ElevationUiEvent.ChangeElevation(it))
                            }) {
                                Text(it.value.toString())
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangingRateControlPanel(viewModel: MainViewModel) {
    var contentVisible by rememberSaveable { mutableStateOf(false) }
    val list = Rate.entries.toTypedArray()
    val state by viewModel.stateChangingRate.collectAsState(null)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedButton(onClick = { contentVisible = !contentVisible }) {
            Row {
                Image(
                    painter = painterResource(if (contentVisible) R.drawable.expand_less else R.drawable.expand_more),
                    contentDescription = "Expand more"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (contentVisible) "Hide Changing rate control" else "Show Changing rate control")
            }
        }

        AnimatedVisibility(contentVisible) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text("Current elevation: ${state?.value?.value ?: ""}")
                ElevatedButton(onClick = {
                    viewModel.changingRateControl(ChangingRateUiEvent.GetChangingRate)
                }) {
                    Text("Get changing rate")
                }
                Spacer(modifier = Modifier.height(8.dp))

                ElevatedButton(onClick = {
                    viewModel.changingRateControl(ChangingRateUiEvent.ChangeChangingRate(Rate.HZ_07))
                }) {
                    Text("${Rate.HZ_07.value} (Default)")
                }
                Spacer(modifier = Modifier.height(8.dp))

                list
                    .filter { it != Rate.HZ_07 }
                    .chunked(3)
                    .forEachIndexed { position, list ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            list.forEach {
                                ElevatedButton(onClick = {
                                    viewModel.changingRateControl(
                                        ChangingRateUiEvent.ChangeChangingRate(
                                            it
                                        )
                                    )
                                }) { Text(it.value.toString()) }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                        }
                    }
            }
        }
    }
}