package com.vigram.sdkgh1.compose.event

sealed class ConfigurationEvent {
    data class InProgress(val message: String) : ConfigurationEvent()
    data class Error(val message: String) : ConfigurationEvent()
    object Done : ConfigurationEvent()
}