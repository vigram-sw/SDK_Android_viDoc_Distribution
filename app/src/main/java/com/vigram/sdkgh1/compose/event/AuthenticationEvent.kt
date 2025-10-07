package com.vigram.sdkgh1.compose.event

sealed class AuthenticationEvent {
    object Start : AuthenticationEvent()
    object Success : AuthenticationEvent()
    data class Error(val message: String) : AuthenticationEvent()
}