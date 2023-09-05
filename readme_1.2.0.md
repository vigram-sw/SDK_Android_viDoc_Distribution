# ``VigramSDK``

##

## What is the new on 1.2.0

### Software service
If you need to get information about all available software versions, create a SoftwareService object and use the getAllAvailableSoftware() method:
```kotlin
val softwareService = Vigram.softwareService()
softwareService.getAllAvailableSoftware { versionsList ->
    versionsList.forEach { version ->
        Log.i("Version", "Available version: $version")
    }
}
```

### Peripheral
#### Added the ability to update the viDoc software.
To install the software, use the `setUpdateSoftwareToNextStartup` method of the `Peripheral` class:
```kotlin
peripheral.setUpdateSoftwareToNextStartup(version)
```

If needed to check if a software update is needed use the `softwareNotification` method. If invoke is true, the update to the current version will start when reconnecting to viDoc:

```kotlin
peripheral.softwareNotification { actualVersion, currentVersion, callAnswer ->
	callAnswer.invoke(true)	
}
```
After reload viDoc is begining update software process. Use the following `progressUpdateSoftware` method to control the upgrade process:
```kotlin
peripheral.progressUpdateSoftware({ state, message ->
	Log.i("Update status", "$state: $message")
}, 
{ progress ->
	Log.i("Update progress", "$progress")
})
```
The `state` is an instance of the `StateUpdateSoftware` enumeration. This enumeration contains the following states:`START_UPDATE`, `END_UPDATE`, `ERROR_UPDATE`. The `message` is a string variable that may contain some information about the update status. The `progress` is a double variable that represents the update progress as a percentage.

#### Satellite messages

- Using the `satelliteMessages` method, you can receive: NAV-PVT message (the numbers of satellite, north, east and down value velocities), NAV-DOP message (HDOP, VDOP, PDOP values), acknowledged message, current elevation, current rate of change of messages, information about the GNSS status of the current satellite, current dynamic state:
```kotlin
peripheral.satelliteMessages { satelliteMessage ->
	Log.i("DOP value", "${satelliteMessage.dop}")
	Log.i("PVT value", "${satelliteMessage.pvt}")
	Log.i("Ack message result", "${satelliteMessage.acknowledgedMessage?.getResult()}")
	Log.i("Current elevation", "${satelliteMessage.elevation}")
	Log.i("Current changing rate", "${satelliteMessage.changingRate}")
	Log.i("Satellite status", "${satelliteMessage.statusSatellite?.satelliteType} - ${satelliteMessage.statusSatellite?.isEnabled}")
	Log.i("Current dynamic state", "${satelliteMessage.dynamicState}")
}
```

- To enable or disable NAV-DOP messages, use the `changeStatusNAVDOP` method:
```kotlin
peripheral.changeStatusNAVDOP(activate = true) { ack ->
	Log.i("Result", "acknowledged message result: ${ack.getResult()}")
}
```

- To enable or disable NAV-PVT messages, use the `changeStatusNAVPVT` method:
```kotlin
peripheral.changeStatusNAVPVT(activate = true) { ack ->
	Log.i("Result", "acknowledged message result: ${ack.getResult()}")
}
```

- To get current elevation, use the `getCurrentMinimumElevation` method:
```kotlin
peripheral.getCurrentMinimumElevation { elevation ->
	Log.i("Current elevation", "$elevation")
}
```

- To get the current rate of change of messages, use the `getChangingRateOfMessages ` method:
```kotlin
peripheral.getChangingRateOfMessages { rate ->
	Log.i("Current rate", "$rate")
}
```

- To get the current dynamic state, use the `getCurrentDynamicState ` method:
```kotlin
peripheral.getCurrentDynamicState { state ->
	Log.i("Current state", "$state")
}
```

- To get the current GNSS status of the current satellite, use the `getCurrentStatusGNSS ` method:
```kotlin
peripheral.getCurrentStatusGNSS(NavigationSystemType.GLONASS) { status ->
	Log.i("Current state", "Current state of ${status.satelliteType} is ${status.isEnabled}")
}
```

#### Changing the configuration

- To set the elevation, use the `setMinimumElevation` method, to which you need to pass an instance of the `Elevation` enum class:
```kotlin
peripheral.setMinimumElevation(Elevation.ANGLE_0) { ack ->
	Log.i("Result", "The result of setting the elevation: ${ack.getResult()}")
}
```

- To set the rate of change of the messages, use the `setChangingRateOfMessages` method, to which you need to pass an instance of the `RateValue` enumeration class:
```kotlin
peripheral.setChangingRateOfMessages(RateValue.HZ_01) { ack ->
	Log.i("Result", "The result of changing the rate of change of messages: ${ack.getResult()}")
}
```

- To enable/disable of GNSS Constellation to selected satellite, use the `changeStatusGNSS` method, method, to which you need to pass an instance of the `NavigationSystemType` enumeration class:
```kotlin
peripheral.changeStatusGNSS(NavigationSystemType.GLONASS, true) { ack ->
	Log.i("Result", "The result of changing the status: ${ack.getResult()}")
}
```

- To set the dynamic state, use the `setDynamicState` method, method, to which you need to pass an instance of the `DynamicState` enumeration class:
```kotlin
peripheral.setDynamicState(DynamicState.PEDESTRIAN) { ack ->
	Log.i("Result", "The result of changing the dynamic state: ${ack.getResult()}")
}
```

- To ctivate GNSS Constellation for all satellite, use `activateAllConstellationGNSS` method:
```kotlin
peripheral.activateAllConstellationGNSS { ack ->
	Log.i("Result", "Result of activation of all GNSS constellations: ${ack.getResult()}")
}
```

### Peripheral logger
If you need use `PeripheralLogger` first create URL path for logger file. For example:

```kotlin
val pathLog = context?.filesDir?.path + "/" +
	SimpleDateFormat("dd.MM.yyyy-hh-mm-ss").format(Date()) + ".txt"

val fileLog = File(pathLog)
val uri = Uri.fromFile(fileLog)
val logger = peripheralLogger(uri)

```

After then, need to pass url path of logger file to peripheral init:

```kotlin
peripheral = Vigram.peripheral(
	peripheral = bluetoothDevice, 
	peripheralLogger = logger
)

```
Note: If you don’t pass url path to init, logger will not works

The logger registered all activities and write its in file.

### Peripheral Configuration

- If you need to disable some of the GNSS messages or change the rate of changing message or minimum elevation value use in navigation parameters, create a variable of type `PeripheralConfiguration` that includes the configuration data and receive this parameter to the peripheral initializer.

Example:

```kotlin
val peripheralConfiguration = PeripheralConfiguration(
	rateOfChangeMessages = RateValue.HZ_01,
	navDOPActivate = false,
	navPVTActivate = false
)
```
After then, need to pass configuration to peripheral init:

```kotlin
// If use logger
peripheral = Vigram.peripheral(
	peripheral = bluetoothDevice,
	peripheralLogger = logger,
	peripheralConfiguration = peripheralConfiguration
)

// If not use logger
peripheral = Vigram.peripheral(
	peripheral = bluetoothDevice,
	peripheralConfiguration = peripheralConfiguration
)
// If not use configuration and logger
peripheral = Vigram.peripheral(peripheral = bluetoothDevice)
```

Note: If any configuration parameter has not been set or the configurator has not been passed to the periphery, the periphery will be configured by default.

```kotlin
// Rate of changing message is set 7 hertz
val rateOfChangeMessages: RateValue = RateValue.HZ_07,

// Minimum elevation value using in navigation is set 10 degree
val elevationValue: Elevation = Elevation.ANGLE_10,

// All satellite constellation is active
val glonassActivate: Boolean = true,
val beidouActivate: Boolean = true,
val galileoActivate: Boolean = true,
val qzssActivate: Boolean = true,
val sbassActivate: Boolean = true,

// NAV-DOP and NAV-PVT Messages is active
val navDOPActivate: Boolean = true,
val navPVTActivate: Boolean = true,

// Dynamic state mode
val dynamicType: DynamicState = DynamicState.PEDESTRIAN
```

- To find out the status of the peripheral configuration, use the method:

```kotlin
peripheral.configurationState { state ->
	Log.e("State Configuration", "${state.name}")
}
```

This method returns an element of the `StatePeripheralConfiguration` enum class:

```kotlin
enum class StatePeripheralConfiguration {
    // The configuration is done
    DONE,

    // The configuration is in progress
    IN_PROGRESS,

    // Configuration failed
    FAILED;
}
```

### SinglePoint

Added the ability to use dynamic state of viDoc for single point measurent.

If you need to use pedestrian or stationary dynamic state mode viDoc with SinglePoint measurements use this init
```kotlin
val singlePointRecordingService = Vigram.singlePointRecordingService(
	gpsService = gpsService,
	laserService = laserService,
	laserConfiguration = configuration,
	dynamicStateType = DynamicState.STATIONARY
)
```

If you need to use pedestrian or stationary dynamic state mode viDoc with SinglePoint measurements with a fixed height use this init
```kotlin
val singlePointRecordingService = Vigram.singlePointRecordingService(
	gpsService = gpsService,
	constantHeight = height,
	dynamicStateType = DynamicStateType.STATIONARY
)
```

This state will be set during the measurement. By default, the state is stationary. After the measurement, the state will switch to pedestrian.

You should use an instance of the `DynamicState` enumeration class:

```kotlin
enum class DynamicState {
	PEDESTRIAN,
	STATIONARY
}
```

### Authentication

- If you need to check the token validation, you can use the `isTokenValid` method at any time ___after___ the initial verification. This method returns an instance of the `AuthenticationData` class that contains information about the authorization result:
```kotlin
Vigram.init(this, token).check { authenticationData -> 
	Log.i("Authorization result", "$authenticationData")
}
```
```kotlin
val authenticationData = Vigram.tokenIsValid()
```

### Fixed issues
- Fixed incorrect return of battery charge value and software version in some cases
- Fixed the functioning of the `data` method of `NtripTask`. The signature of the data method has been changed:
```kotlin
fun data(callback: Callback1<ByteArray>)
```
- Fixed the functioning of the timestampedCoordinate method of the GPSService interface. The signature of the timestampedCoordinate method has been changed:
```kotlin 
fun timestampedCoordinate(callback: Callback2<Date?, GPSCoordinate>)
```