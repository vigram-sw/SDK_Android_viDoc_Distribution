package com.vigram.sdkgh1.compose.event

sealed class PeripheralEvent {
    object Connected : PeripheralEvent()
    object Disconnected : PeripheralEvent()
}