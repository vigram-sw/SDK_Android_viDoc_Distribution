package com.vigram.sdkgh1.data.mapper

import com.vigram.sdk.Models.Device.Baudrate as VigramBaudrate
import com.vigram.sdkgh1.domain.entity.Baudrate as DemoBaudrate

fun DemoBaudrate.toBaudrateSDK(): VigramBaudrate {
    return when (this) {
        DemoBaudrate.Standart -> VigramBaudrate.standart
        DemoBaudrate.Safe -> VigramBaudrate.safe
        DemoBaudrate.Update -> VigramBaudrate.update
    }
}