# ``VigramSDK``

## What is the new on 1.2.0-beta3

### Peripheral Logger

If you need use ``PeripheralLogger`` first create URL path for logger file. For example:

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

- If you need to disable some of the GNSS messages or change the rate of changing message or minimum elevation value use in navigation parameters, create a variable of type ``PeripheralConfiguration`` that includes the configuration data and receive this parameter to the peripheral initializer.

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
val dynamicType: DynamicStateType = DynamicStateType.PEDESTRIAN
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


### Satellite Message

- Added rawx field containing UBX-RXM-RAWX message
- Added sfrbx field containing UBX-RXM-SFRBX message
- Added dynamicState field containing information about the current dynamic state of the message
- Added changingRate field containing information about the current changing rate value

Thus, in this version, the ``SatelliteMessage`` class contains the fields:


```kotlin
    var dop: Dop? = null
    var pvt: Pvt? = null
    var acknowledgedMessage: ACKMessage? = null
    var elevation: Elevation? = null
    var navigationSystemStatus: StatusSatellite? = null
    var rawx: RAWX? = null
    var sfrbx: SFRBX? = null
    var dynamicState: DynamicStateType? = null
    var changingRate: RateValue? = null
```

Since 1.2.0-beta1.2 you can receive satelliteMessage using the satelliteMessages method: 
```kotlin
peripheral.satelliteMessages { satelliteMessage ->
	// code //
}
```
### SinglePoint

- Added the ability to use pedestrian state of viDoc for SinglePoint measurent. 

If you need to use pedestrian or stationary dynamic state mode viDoc with SinglePoint measurements use this init

```kotlin
val singlePointRecordingService = Vigram.singlePointRecordingService(
	gpsService = gpsService,
	laserService = laserService,
	laserConfiguration = configuration,
	dynamicStateType = DynamicStateType.STATIONARY
)
```
This state will be set during the measurement. By default, the state is stationary. After the measurement, the state will switch to pedestrian.

You should use DynamicState Type enum class to set the dynamic state during the recording:

```kotlin
enum class DynamicStateType {
	PEDESTRIAN,
	STATIONARY
}
```


In addition to the above, some fixes were implemented
