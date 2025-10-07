package com.vigram.sdkgh1.compose.event

sealed class SoftwareUpdateEvent {
    data class Updating(val progress: Double) : SoftwareUpdateEvent()
    object End : SoftwareUpdateEvent()
    data class Error(val message: String) : SoftwareUpdateEvent()
    object None : SoftwareUpdateEvent()
}