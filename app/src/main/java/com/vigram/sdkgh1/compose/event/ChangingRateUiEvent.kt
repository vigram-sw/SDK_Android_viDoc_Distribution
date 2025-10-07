package com.vigram.sdkgh1.compose.event

import com.vigram.sdk.Models.Satellite.Rate

sealed class ChangingRateUiEvent {
    data class ChangeChangingRate(val value: Rate) : ChangingRateUiEvent()
    object GetChangingRate : ChangingRateUiEvent()
}