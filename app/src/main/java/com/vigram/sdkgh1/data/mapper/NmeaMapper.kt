package com.vigram.sdkgh1.data.mapper

import com.vigram.sdkgh1.domain.entity.GgaData
import com.vigram.sdkgh1.domain.entity.GstData
import com.vigram.sdkgh1.domain.entity.MainNmeaMessage
import com.vigram.sdk.Models.GGAMessage
import com.vigram.sdk.Models.GSTMessage
import com.vigram.sdk.Modules.NmeaMessage


fun NmeaMessage.toMainNmeaMessage(): MainNmeaMessage {
    return when (this) {
        is NmeaMessage.GNGGA -> MainNmeaMessage.GNGGA(value.toGgaSDK())
        is NmeaMessage.GNGST -> MainNmeaMessage.GNGST(value.toGstSDK())
        is NmeaMessage.TXT -> MainNmeaMessage.TXT(value.raw)
    }
}


fun GGAMessage.toGgaSDK(): GgaData {
    return GgaData(
        raw = raw,
        time = time?.description ?: "-",
        latitude = location.latitude?.toString() ?: "-",
        longitude = location.longitude?.toString() ?: "-",
        quality = quality?.name ?: "-",
        referenceAltitude = referenceAltitude.toString(),
        geoidSeparation = geoidSeparation.toString(),
        correctionAge = correctionAge.toString(),
        correctionStationID = correctionStationID.toString(),
        satelliteCount = satelliteCount.toString(),
        hdop = hdop.toString()
    )
}

fun GSTMessage.toGstSDK(): GstData {
    return GstData(
        rms = rms?.toString() ?: "-",
        semiMajor1SigmaError = semiMajor1SigmaError?.toString() ?: "-",
        semiMinor1SigmaError = semiMinor1SigmaError?.toString() ?: "-",
        errorEllipseOrientation = errorEllipseOrientation?.toString() ?: "-",
        latitudeError = latitudeError.toString(),
        longitudeError = longitudeError.toString(),
        altitudeError = altitudeError.toString(),
        accuracyHorizontal = accuracy?.horizontal?.toString() ?: "-",
        accuracyVertical = accuracy?.vertical?.toString() ?: "-"
    )
}