package com.vigram.sdkgh1.domain.entity

sealed class MainNmeaMessage {
    data class GNGGA(val value: GgaData) : MainNmeaMessage()
    data class GNGST(val value: GstData) : MainNmeaMessage()
    data class TXT(val value: String): MainNmeaMessage()
}