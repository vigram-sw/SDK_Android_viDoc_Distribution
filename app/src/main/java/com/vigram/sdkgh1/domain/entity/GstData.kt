package com.vigram.sdkgh1.domain.entity

data class GstData (
    val rms: String = "-",
    val semiMajor1SigmaError: String  = "-",
    val semiMinor1SigmaError: String  = "-",
    val errorEllipseOrientation: String  = "-",
    val latitudeError: String  = "-",
    val longitudeError: String  = "-",
    val altitudeError: String  = "-",
    val accuracyHorizontal: String = "-",
    val accuracyVertical: String = "-"
)