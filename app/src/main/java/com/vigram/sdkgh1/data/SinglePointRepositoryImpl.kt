package com.vigram.sdkgh1.data

import android.util.Log
import com.vigram.sdk.GPS.GPSService
import com.vigram.sdk.Laser.LaserConfiguration
import com.vigram.sdk.Laser.LaserService
import com.vigram.sdk.Models.RequestResult
import com.vigram.sdk.Models.SinglePoint
import com.vigram.sdk.NTRIP.NtripTask
import com.vigram.sdk.Peripheral.Peripheral
import com.vigram.sdk.SinglePoint.SinglePointRecordModel
import com.vigram.sdk.Vigram
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow

object SinglePointRepositoryImpl {

//    private val laserService: LaserRepositoryImpl = LaserRepositoryImpl
//
//    @Volatile
//    private var _gpsService: GPSService? = null
//
//    val gpsService: GPSService
//        get() = _gpsService
//            ?: throw IllegalStateException("GpsService is not initialized")
//
//    private var ntripTask: NtripTask? = null
//
//    fun init(peripheral: Peripheral, ntripTask: NtripTask) {
//        if (_gpsService == null) {
//            synchronized(this) {
//                if (_gpsService == null) {
//                    _gpsService = Vigram.gpsService(peripheral, ntripTask)
//                }
//            }
//        }
//
//        this.ntripTask = ntripTask
//    }
//
//
//    private val _singlePointRecordingResult =
//        MutableSharedFlow<RequestResult<SinglePointRecordModel<SinglePoint>>>()
//    val singlePointRecordingResult: SharedFlow<RequestResult<SinglePointRecordModel<SinglePoint>>> =
//        _singlePointRecordingResult.asSharedFlow()
//
//
//    fun startMeasurement(
//        laserConfiguration: LaserConfiguration,
//        ntripTask: NtripTask? = null,
//    ): Flow<RequestResult<SinglePointRecordModel<SinglePoint>>> =
//        callbackFlow {
//            if (ntripTask != null) this@SinglePointRepositoryImpl.ntripTask = ntripTask
//
//            val singlePoint =
//                Vigram.singlePointRecordingService(
//                    gpsService = gpsService,
//                    laserService = laserService.laserService,
//                    laserConfiguration = laserConfiguration
//                )
//
//            singlePoint.record(laserConfiguration.duration, {
//                Log.e("SinglePoint", "single: $it")
//            }, {
//                Log.e("SinglePoint", "average: $it")
//                trySend(it).isSuccess
//            })
//
//            awaitClose {}
//        }
}