package com.vigram.sdkgh1.domain.repository

import com.vigram.sdk.Models.Device.DeviceMessage
import com.vigram.sdk.Models.Device.LaserState
import com.vigram.sdk.Models.RequestResult
import com.vigram.sdk.Peripheral.Peripheral
import com.vigram.sdkgh1.domain.entity.MainLaserConfiguration
import com.vigram.sdkgh1.domain.entity.MainLaserPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LaserRepository {
    fun init(peripheral: Peripheral)
    fun laserOn(laserPosition: MainLaserPosition): Flow<RequestResult<*>>
    fun laserOff(laserPosition: MainLaserPosition): Flow<RequestResult<*>>
    fun getLaserStatus(): Flow<RequestResult<LaserState>>
    fun startLaserMeasurement(laserConfiguration: MainLaserConfiguration): Flow<RequestResult<DeviceMessage.Measurement>>

}