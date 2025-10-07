package com.vigram.sdkgh1.data

import com.vigram.sdkgh1.data.mapper.toNtripConnectionInformationSDK
import com.vigram.sdkgh1.domain.entity.NtripConnectionData
import com.vigram.sdkgh1.domain.repository.NtripRepository
import com.vigram.sdk.Models.GGAMessage
import com.vigram.sdk.Models.NtripState
import com.vigram.sdk.Models.RequestResult
import com.vigram.sdk.NTRIP.NtripConnectionInformation
import com.vigram.sdk.NTRIP.NtripMountPoint
import com.vigram.sdk.NTRIP.NtripTask
import com.vigram.sdk.Peripheral.Peripheral
import com.vigram.sdk.Vigram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class NtripRepositoryImpl(
    private val scope: CoroutineScope,
) : NtripRepository {

    private val ntripService = Vigram.ntripService()

    @Volatile
    private var _ntripTask: NtripTask? = null

    override val ntripTask: NtripTask
        get() = _ntripTask ?: throw IllegalStateException("NtripTask is not initialized")

    override fun startTask(
        peripheral: Peripheral,
        ntripInformation: NtripConnectionData,
        message: GGAMessage,
    ) {
        if (_ntripTask == null) {
            synchronized(this) {
                if (_ntripTask == null) {
                    _ntripTask = ntripService.task(
                        ntripInformation.toNtripConnectionInformationSDK(),
                        ntripInformation.mount,
                        message
                    )

                    val gpsService = Vigram.gpsService(peripheral, ntripTask)

                    gpsService.start {
                        if (it == NtripState.Disconnected) {
                            _ntripTask = null
                        }
                        scope.launch {
                            _ntripState.emit(it)
                        }
                    }

                    ntripTask.data {
                        scope.launch {
                            _data.emit(it)
                        }
                    }
                }
            }
        }
    }

    override fun mountpoints(ntripConnectionInformation: NtripConnectionInformation)
            : Flow<RequestResult<List<NtripMountPoint>>> = callbackFlow {
        ntripService.mountpoints(ntripConnectionInformation) {
            trySend(it).isSuccess
        }
        awaitClose {}
    }

    private val _ntripState: MutableStateFlow<NtripState> =
        MutableStateFlow(NtripState.Disconnected)
    override val ntripState: SharedFlow<NtripState> get() = _ntripState.asStateFlow()

    override val mountPoint: String? get() = ntripTask.mountPoint

    override val isConnect: Boolean get() = ntripTask.isConnect

    private val _data: MutableStateFlow<ByteArray> = MutableStateFlow(byteArrayOf())
    override val data: StateFlow<ByteArray> get() = _data.asStateFlow()

    override suspend fun disconnect() = ntripTask.cancel()

    override suspend fun setGGA(ggaMessage: GGAMessage) {
        ntripTask.setGGA(ggaMessage)
    }
}