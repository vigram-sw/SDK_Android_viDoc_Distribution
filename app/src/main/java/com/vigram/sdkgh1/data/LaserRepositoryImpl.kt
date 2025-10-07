package com.vigram.sdkgh1.data

import com.vigram.sdk.Laser.LaserService
import com.vigram.sdk.Models.Device.DeviceMessage
import com.vigram.sdk.Models.Device.LaserState
import com.vigram.sdk.Models.RequestResult
import com.vigram.sdk.Peripheral.Peripheral
import com.vigram.sdk.Vigram
import com.vigram.sdkgh1.data.mapper.toLaserConfigurationSDK
import com.vigram.sdkgh1.data.mapper.toLaserPositionSDK
import com.vigram.sdkgh1.domain.entity.MainLaserConfiguration
import com.vigram.sdkgh1.domain.entity.MainLaserPosition
import com.vigram.sdkgh1.domain.repository.LaserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class LaserRepositoryImpl(
    private val scope: CoroutineScope,
) : LaserRepository {
    @Volatile
    private var _laserService: LaserService? = null

    val laserService: LaserService
        get() = _laserService
            ?: throw IllegalStateException("BluetoothService is not initialized")

    override fun init(peripheral: Peripheral) {
        if (_laserService == null) {
            synchronized(this) {
                if (_laserService == null) {
                    _laserService = Vigram.laserService(peripheral = peripheral)
                }
            }
        }
    }

    override fun laserOn(position: MainLaserPosition): Flow<RequestResult<*>> =
        callbackFlow {
            laserService.turnLaserOn(position.toLaserPositionSDK()) { result ->
                trySend(result).isSuccess
            }
            awaitClose {}
        }

    override fun laserOff(position: MainLaserPosition): Flow<RequestResult<*>> =
        callbackFlow {
            laserService.turnLaserOff(position.toLaserPositionSDK()) { result ->
                trySend(result).isSuccess
            }
            awaitClose {}
        }

    override fun getLaserStatus(): Flow<RequestResult<LaserState>> =
        callbackFlow {
            laserService.getLasersStatus {
                trySend(it).isSuccess
            }
            awaitClose {}
        }


    override fun startLaserMeasurement(laserConfiguration: MainLaserConfiguration): Flow<RequestResult<DeviceMessage.Measurement>> =
        callbackFlow {
            laserService.record(laserConfiguration.toLaserConfigurationSDK()) { result ->
                trySend(result).isSuccess
            }
            awaitClose {}
        }
}