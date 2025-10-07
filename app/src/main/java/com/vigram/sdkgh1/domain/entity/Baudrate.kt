package com.vigram.sdkgh1.domain.entity

/** The Baud rate viDoc */
enum class Baudrate {
    /** Standard baud rate (115200 Baud) */
    Standart,
    /** Baud rate after software update (38400 Baud) */
    Update,
    /** Baud rate when a safe boot was performed (9600 Baud) */
    Safe
}