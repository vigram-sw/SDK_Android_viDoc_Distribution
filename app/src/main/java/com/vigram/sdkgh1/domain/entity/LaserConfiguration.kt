package com.vigram.sdkgh1.domain.entity

data class MainLaserConfiguration(
    val position: MainLaserPosition,
    val shotMode: MainLaserShotMode,
    val duration: Int
)

enum class MainLaserPosition {
    Bottom,
    Front
}

enum class MainLaserShotMode {
    Slow,
    Fast,
    Auto
}