package com.vigram.sdkgh1.compose.event

import com.vigram.sdk.Models.Satellite.Elevation


// UI события elevation
sealed class ElevationUiEvent {
    data class ChangeElevation(val value: Elevation) : ElevationUiEvent()
    object GetElevation : ElevationUiEvent()
}