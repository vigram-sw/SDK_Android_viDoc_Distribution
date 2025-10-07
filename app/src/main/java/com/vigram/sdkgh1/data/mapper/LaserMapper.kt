package com.vigram.sdkgh1.data.mapper

import com.vigram.sdk.Laser.LaserConfiguration
import com.vigram.sdkgh1.domain.entity.MainLaserConfiguration
import com.vigram.sdkgh1.domain.entity.MainLaserPosition
import com.vigram.sdkgh1.domain.entity.MainLaserShotMode


fun MainLaserConfiguration.toLaserConfigurationSDK(): LaserConfiguration {
    return LaserConfiguration(
        position = position.toLaserPositionSDK(),
        shotMode = shotMode.toLaserShotMode(),
        duration = duration
    )
}

fun MainLaserPosition.toLaserPositionSDK(): LaserConfiguration.Position {
    return when (this) {
        MainLaserPosition.Bottom -> LaserConfiguration.Position.Bottom
        MainLaserPosition.Front -> LaserConfiguration.Position.Front
    }
}

fun MainLaserShotMode.toLaserShotMode(): LaserConfiguration.ShotMode {
    return when (this) {
        MainLaserShotMode.Slow -> LaserConfiguration.ShotMode.Slow
        MainLaserShotMode.Fast -> LaserConfiguration.ShotMode.Fast
        MainLaserShotMode.Auto -> LaserConfiguration.ShotMode.Auto
    }
}
