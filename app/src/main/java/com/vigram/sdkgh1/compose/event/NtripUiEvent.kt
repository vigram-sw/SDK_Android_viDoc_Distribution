package com.vigram.sdkgh1.compose.event

import com.vigram.sdk.NTRIP.NtripConnectionInformation


sealed class NtripUiEvent {
    data class GetMounts(val ntripConnectionInformation: NtripConnectionInformation): NtripUiEvent()
}