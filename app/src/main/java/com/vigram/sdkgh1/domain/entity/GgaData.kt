package com.vigram.sdkgh1.domain.entity

data class GgaData(
    val raw: String = "-",
    val time: String = "-",
    val latitude: String = "-",
    val longitude: String = "-",
    val quality: String = "-",
    val referenceAltitude: String = "-",
    val geoidSeparation: String = "-",
    val correctionAge: String = "-",
    val correctionStationID: String = "-",
    val satelliteCount: String = "-",
    val hdop: String = "-"
)