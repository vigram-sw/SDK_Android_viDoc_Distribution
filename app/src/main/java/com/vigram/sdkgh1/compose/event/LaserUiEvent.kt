package com.vigram.sdkgh1.compose.event

import com.vigram.sdkgh1.domain.entity.MainLaserConfiguration
import com.vigram.sdkgh1.domain.entity.MainLaserPosition

sealed class LaserUiEvent {
    data class LaserOn(val laserPosition: MainLaserPosition) : LaserUiEvent()
    data class LaserOff(val laserPosition: MainLaserPosition) : LaserUiEvent()
    data class Measurement(val laserPosition: MainLaserConfiguration) : LaserUiEvent()
    object LaserStatus : LaserUiEvent()
}