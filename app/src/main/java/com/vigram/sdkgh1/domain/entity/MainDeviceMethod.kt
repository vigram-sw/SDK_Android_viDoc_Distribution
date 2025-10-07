package com.vigram.sdkgh1.domain.entity

import com.vigram.sdk.Laser.LaserConfiguration

sealed class MainDeviceMethod {

    object GetVersionNumbers : MainDeviceMethod()

    object GetBattery : MainDeviceMethod()
    object GetSerialNumber : MainDeviceMethod()

    data class CalibrationIMU(val auto: Boolean) : MainDeviceMethod()

    object GetIMUCalibrationStatus : MainDeviceMethod()
    object SwitchProtocol : MainDeviceMethod()
    object ReadIMUAngle : MainDeviceMethod()
    object ReadIMUAcc : MainDeviceMethod()
    object ReadIMURotation : MainDeviceMethod()
    object ReadIMURotationRaw : MainDeviceMethod()
    object ReadIMUMagnetic : MainDeviceMethod()
    object ReadIMUTemp : MainDeviceMethod()
    object GetHardwareIndex : MainDeviceMethod()
    object ResetUblox : MainDeviceMethod()

    data class ChangeBaudrate(val baudrate: Baudrate) : MainDeviceMethod()

    sealed class LaserCommand : MainDeviceMethod() {
        object LaserStatus : LaserCommand()
        data class Measurement(
            val mode: LaserConfiguration.ShotMode,
            val position: MainLaserPosition,
            val duration: Int,
        ) : LaserCommand()

        data class TurnOn(val position: MainLaserPosition) : LaserCommand()
        data class TurnOff(val position: MainLaserPosition) : LaserCommand()
    }
}