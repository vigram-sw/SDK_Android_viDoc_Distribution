# ``VigramSDK``

##
![VigramSDKLogoDocumentation](https://vigram.com/wp-content/uploads/2021/04/vigram_smart_documentation_compressed_black.svg)



## What is the new on 1.2.0-beta1.2


### SoftwareService

If you need to get information about all available software versions, create a ``SoftwareService`` object and use the ``getAllAvailableSoftware() ``method:

```kotlin
val softwareService = SoftwareService()
        
var versions: List<DeviceMessage.Software>

softwareService.getAllAvailableSoftware { 
	versions = it
}
```

### Peripheral

- Added the ability to update the viDoc software.

If needed to check if a software update is needed use the ``softwareNotification()`` method. If invoke is true, the update to the current version will start when reconnecting to viDoc.

```kotlin
peripheral.softwareNotification { actualVersion, currentVersion, callAnswer ->
	callAnswer.invoke(true)	
}
```

If you need to custom install the software version, use the ``setUpdateSoftwareToNextStartup`` method:
```kotlin
peripheral.setUpdateSoftwareToNextStartup(versions[0])
```
After reload viDoc is begining update software process.

Use the following method to control the upgrade process.
```kotlin
peripheral.progressUpdateSoftware({ state, message ->
	// code //
}, 
{ progress ->
	// code //
})
```
The ``state`` is an instance of the ``StateUpdateSoftware`` enumeration. This enumeration contains the following states:``START_UPDATE``, ``END_UPDATE``, ``ERROR_UPDATE``. The ``message`` is a string variable that may contain some information about the update status. The ``progress`` is a double variable that represents the update progress as a percentage.

- Satellite messages

Using the ``satelliteMessages`` method, you can receive: GNSS Constellation information, elevation value use in navigation, NAV-PVT message (the numbers of satellite, north, east and down value velocities), NAV-DOP message (HDOP, VDOP, PDOP values). 
```kotlin
peripheral.satelliteMessages { satelliteMessage ->
	// code //
}
```


- GNSS constellation enable/disable commands for each NavigationSystemType have been added to the Peripheral

If needed to get the status for the selected satellite perform the ``getCurrentStatusGNSS`` method y passing the ``NavigationSystemType`` enumeration element to it.

Note: GPS type is not disable.

```kotlin
peripheral.getCurrentStatusGNSS(NavigationSystemType.BEI_DOU){ statusSatellite ->
	// code //
}
```

If needed activate all constellation perform the method

```kotlin
peripheral.activateAllConstellationGNSS { ackMessage ->
	// code //
}
```


- Added command to set minimum elevation for satellites to be used in navigation. If needed to get the current elevation value used in navigation perform the method
```kotlin
peripheral.getCurrentMinimumElevation { elevation ->
	// code //
}
```

If needed to set the current elevation value used in navigation perform the ``setMinimumElevation`` method by passing the ``Elevation`` enumeration element to it.

```kotlin
peripheral.setMinimumElevation(Elevation.ANGLE_0) { ackMessage ->
	// code //
}
```

- Add enable/disable commands for NAV-DOP messages If needed to enable/disable NAV-DOP messages perform the method
```kotlin
peripheral.changeStatusNAVDOP(activate = false, permanently = false){ ackMessage ->
	// code //
}
```
Permanently: Defines the storage of parameters in permanent memory (Flash)

- Add enable/disable commands for NAV-PVT messages If needed to enable/disable NAV-PVT messages perform the method
```kotlin
peripheral.changeStatusNAVPVT(activate = true, permanently = false) { ackMessage ->
	// code //
}
```
Permanently: Defines the storage of parameters in permanent memory (Flash)

- Add changing rate of navigation message commands If needed to change rate of navigation messages perform the method by passing the ``RateValue`` enumeration element to it.

```kotlin
peripheral?.setChangingRateOfMessages(RateValue.HZ_07, false) { ackMessage ->
	// code //
}
```
