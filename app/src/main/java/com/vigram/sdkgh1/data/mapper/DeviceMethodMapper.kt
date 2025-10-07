package com.vigram.sdkgh1.data.mapper

import com.vigram.sdkgh1.domain.entity.MainDeviceMethod
import com.vigram.sdk.Models.Device.DeviceMethod

fun MainDeviceMethod.toDeviceMethodSDK(): DeviceMethod {
   return when (this) {
        is MainDeviceMethod.GetVersionNumbers -> DeviceMethod.GetVersionNumbers
        is MainDeviceMethod.CalibrationIMU -> DeviceMethod.CalibrationIMU(auto)
        is MainDeviceMethod.ChangeBaudrate -> DeviceMethod.ChangeBaudrate(baudrate.toBaudrateSDK())
        MainDeviceMethod.GetBattery -> DeviceMethod.BatteryCharge
        MainDeviceMethod.GetHardwareIndex -> DeviceMethod.GetHardwareIndex
        MainDeviceMethod.GetIMUCalibrationStatus -> DeviceMethod.GetIMUCalibrationStatus
        MainDeviceMethod.GetSerialNumber -> DeviceMethod.GetSerialNumber
        MainDeviceMethod.LaserCommand.LaserStatus -> DeviceMethod.LaserStatus
        is MainDeviceMethod.LaserCommand.Measurement ->
            DeviceMethod.Measurement(mode , position.toLaserPositionSDK(), duration)

        is MainDeviceMethod.LaserCommand.TurnOff ->
            DeviceMethod.TurnOff(position.toLaserPositionSDK())

        is MainDeviceMethod.LaserCommand.TurnOn ->
            DeviceMethod.TurnOn(position.toLaserPositionSDK())

        MainDeviceMethod.ReadIMUAcc -> DeviceMethod.ReadIMURawDataAcc
        MainDeviceMethod.ReadIMUAngle -> DeviceMethod.ReadIMUAngle
        MainDeviceMethod.ReadIMUMagnetic -> DeviceMethod.ReadIMUMagnetic
        MainDeviceMethod.ReadIMURotation -> DeviceMethod.ReadIMURotationRates
        MainDeviceMethod.ReadIMURotationRaw -> DeviceMethod.ReadIMURotationRawRates
        MainDeviceMethod.ReadIMUTemp -> DeviceMethod.ReadIMUTemp
        MainDeviceMethod.ResetUblox -> DeviceMethod.ResetUblox
        MainDeviceMethod.SwitchProtocol -> DeviceMethod.SwitchProtocol
    }
}