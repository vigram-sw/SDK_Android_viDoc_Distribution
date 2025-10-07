# ``VigramSDK``



## What is the new on 1.2.7


### Authentication
Refactoring token verification
The SDK authentication has been refactoring. The authentication method is remains the same. Usage:

```kotlin
Vigram.init(context, token: yourToken).check { authData ->
	if (authData.success) {
		// Successfull authentication
	} else {
		// Unsuccessful
	}
}
```
Now the SDK offline operation time after successful authentication is equal 14 days. After the first successful authentication, no additional method calls are required. Revalidation of an already authorized token is performed automatically after successfully connecting to the web and launching the SDK.
Note: Endpoint for request: “https://api.zone.vigram.com/tokenService/”

### Peripheral
- Added a new protocol for working with the viDoc.

The protocol version and device version of the viDoc are checked and detected automatically on the SDK side.
- Added variable isNewProtocol - return the version protocol of the viDoc.
```kotlin
peripheral.isNewProtocol()
```

- added requestGetCurrentDevice method - return an instance of the Device class. Usage:
```kotlin
peripheral.requestGetCurrentDevice {
	when (it) {
		is RequestResult.Success -> {
			val device = it.data
			// values:
			// device.typeOfDevice  - get product type of viDoc
			// device.hasFrontLaser - if false - laser is not supported (e.g. viDoc Light)
			// device.hasBottomLaser - if false - laser is not supported (e.g. viDoc Light)
			// device.getHousing  - get current device housing (if available)
			// device.getMount  - get current device mount (if available)
		}
		is RequestResult.Error -> {
			Log.i("Request failed", it.exception.message)
		}
	}
}
```
- Added new enum classes:
```kotlin
enum class DeviceVersion {
    UNKNOWN,
    VIDOC,
    VIDOC_LIGHT,
}

enum class Housings(val description: String) {
    UNKNOWN("unknown"),
    PRINTED_3D("3D printed"),
    ALUMINIUM_BLANK("aluminium blank"),
    ELOXATED_BLACK("black eloxated"),
    PAINTED_BLACK_1("black painted version 1"),
    PAINTED_BLACK_2("black painted version 2"),
    PAINTED_GRAY("grey painted"),
    PAINTED_BLUE("blue painted"),
    PAINTED_RED("red painted");
}

enum class Mounts(val value: String) {
    STANDS("stands for the mount"),
    TABLET("viDoc Tablet (old tablet mount)"),
    SPC("SPC"),
    SPC_PLUS("SPC+"),
    SPC_PLUS_REGULARLY("SPC+ (regularly produced)"),
    SPC_PLUS_BACK_PLATE("SPC+ (backplate replaced)"),
    SPC_PLUS_PROTOTYPE("SPC+ Prototype")
}

enum class HardwareRevisions(val value: String) {
    NO_MINI_PCB("no mini PCB"),
    WITH_MINI_PCB("with mini PCB"),
    STATE_AT_MARKET("state at market entrance"),
    WITH_MINI_PCB_AND_IMU_CALIBRATED("with mini PCB, IMU calibrated")
}
```

- Added message parameter to the PeripheralState enum class. Usage:
```kotlin
peripheral.state { state: PeripheralState ->
	when (state) {
		PeripheralState.Error -> Log.e("Connection check", "${state.message}")
		else -> {...}
	}
}
```

- The TXT message has been added to the NMEA message. Usage:
```kotlin
peripheral.nmea { nmeaMessage ->
	Log.i("TXT", "${nmeaMessage.getTXT()}")
}
```

- Reset viDoc + State Reset. Usage:
```kotlin
peripheral.viDocResetState { state ->
	Log.i("reset state", "$state")
}
peripheral.resetViDoc()
```


### Laser service:
- Added function for requesting laser status
```kotlin
laserService.getLasersStatus { result: RequestResult<DeviceMessage.LaserState> ->
	when (result) {
		is RequestResult.Success ->
			Log.i("success", result.toString())
		is RequestResult.Error ->
			Log.i("error", result.exception.message.toString())
	}
}
```
Laser statuses are defined in the enum class:
```kotlin
enum class LaserState() {
	BOTH_OFF,
	BOTTOM_IS_ON,
	BACK_IS_ON;
}
```

- Refactoring laser measurements
  On the old protocol version, the setting range for the measurement duration was 5…60. There was also no possibility to interrupt a measurement that had already started.
  The new version of the protocol has no restrictions on measurement time.
  Note: If set the value to 0, the measurement will go on infinity. If need to terminate measurements, perform the command to turn off the laser. In new protocol `laserService.turnLaserOff()` method will disabled both lasers in one command. (Available only on the new protocol - HW of viDoc 0.2.1.(12) and up)
