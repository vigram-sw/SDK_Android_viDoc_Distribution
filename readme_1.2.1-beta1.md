# ``VigramSDK``

##

## What is the new on 1.2.1-beta

### SinglePoint

- Added the ability to use pedestrian state of viDoc for single point measurent with a fixed height.

If you need to use pedestrian or stationary dynamic state mode viDoc with SinglePoint measurements with a fixed height use this init

```kotlin
val singlePointRecordingService = Vigram.singlePointRecordingService(
	gpsService = gpsService,
	constantHeight = height,
	dynamicStateType = DynamicStateType.STATIONARY
)
```
This state will be set during the measurement. By default, the state is stationary. After the measurement, the state will switch to pedestrian.


In addition to the above, some fixes were implemented