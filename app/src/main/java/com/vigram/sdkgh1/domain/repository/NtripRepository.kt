package com.vigram.sdkgh1.domain.repository

import com.vigram.sdkgh1.domain.entity.NtripConnectionData
import com.vigram.sdk.Models.GGAMessage
import com.vigram.sdk.Models.NtripState
import com.vigram.sdk.Models.RequestResult
import com.vigram.sdk.NTRIP.NtripConnectionInformation
import com.vigram.sdk.NTRIP.NtripMountPoint
import com.vigram.sdk.NTRIP.NtripTask
import com.vigram.sdk.Peripheral.Peripheral
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface NtripRepository {

    val ntripTask: NtripTask

    fun startTask(
        peripheral: Peripheral,
        ntripInformation: NtripConnectionData,
        message: GGAMessage
    )

    fun mountpoints(
        ntripConnectionInformation: NtripConnectionInformation
    ): Flow<RequestResult<List<NtripMountPoint>>>

    val ntripState: SharedFlow<NtripState>

    val mountPoint: String?

    val isConnect: Boolean

    val data: StateFlow<ByteArray>

    suspend fun disconnect()

    suspend fun setGGA(ggaMessage: GGAMessage)
}