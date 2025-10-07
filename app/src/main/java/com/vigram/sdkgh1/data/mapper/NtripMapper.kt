package com.vigram.sdkgh1.data.mapper

import com.vigram.sdkgh1.domain.entity.NtripConnectionData
import com.vigram.sdk.NTRIP.NtripConnectionInformation

fun NtripConnectionData.toNtripConnectionInformationSDK(): NtripConnectionInformation {
    return NtripConnectionInformation(
        host = host,
        port = port.toIntOrNull() ?: 0,
        username = login,
        password = password,
    )
}