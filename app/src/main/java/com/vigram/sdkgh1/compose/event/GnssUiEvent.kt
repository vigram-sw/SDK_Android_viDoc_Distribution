package com.vigram.sdkgh1.compose.event

import com.vigram.sdk.Models.Satellite.NavigationSystemType

// UI события satellite
sealed class GnssUiEvent {
    data class ChangeSatelliteStatus(val value: NavigationSystemType, val status: Boolean) : GnssUiEvent()
    data class GetSatelliteStatus(val value: NavigationSystemType) : GnssUiEvent()
    object ActivateAllSatellite : GnssUiEvent()
}