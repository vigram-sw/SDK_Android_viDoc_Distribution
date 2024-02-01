# ``VigramSDK``

##

VigramSDK is a library to connect your app to viDoc to retrieve location data and perform laser distance recordings.

For more information, you can view the documentation using this 

## What is the new on 1.2.4
### Single Point Recording Service:
The value measured by the front laser above 20 meters and the value measured by the bottom laser above 40 meters is now considered an error

### Vigram

If you need to check the token validation, you can use the Vigram.isTokenValid() method at any time after the initial verification:

```kotlin
Vigram.init(this, token)
            .check { authenticationData -> /* code */ }

val authenticationData = Vigram.tokenIsValid()
```

### Peripheral



- RXM (SFRBX and RAWX) messages enable/disable commands have been added to the Peripheral

If it is necessary to enable/disable RXM (RAWX and SFRBX) messages, use the ``changeStatusRXM`` method

Note: Laser measurement is not possible with RXM messages enabled.

```kotlin
peripheral?.changeStatusRXM(activate = true, permanently = false)
```

- Recording PPK measurement to file

To start PPK (writing SFRBX and RAWX messages from the device to a file) use the ``startRecordPPKMeasurements`` method

```kotlin
val path = context?.filesDir?.path + "/file_name.ubx"
val file = File(path)
val uri = Uri.fromFile(file)
peripheral?.startRecordPPKMeasurements(uri)
```

To stop PPK use the ``stopRecordPPKMeasurements`` method

```kotlin
peripheral?.stopRecordPPKMeasurements()
```


- Dynamic state

To change the dynamic state of viDoc use the ``setDynamicState`` method

```kotlin
peripheral?.setDynamicState(DynamicStateType.PEDESTRIAN, false) {
    Log.e("Ack message", "${it.getResult()}")
}
```

To get current state of viDoc use the ``getCurrentDynamicState`` method
```kotlin
peripheral?.getCurrentDynamicState {
    Log.e("Current state", "${it.name}")                    
}
```

## Topics

### General

How to create services, change the configuration.


- Use the ``Vigram`` sealed class to create various services, such as a ``BluetoothService``, a ``NtripService``, a ``GPSService`` or a ``LaserService``:
```kotlin
fun init(context: Context, token: String): Authentication

fun bluetoothService(activity: Activity, context: Context): BluetoothService

fun peripheral(peripheral: BluetoothDevice): Peripheral

fun ntripService(): NtripService

fun laserService(peripheral: Peripheral): LaserService

fun gpsService(peripheral: Peripheral,ntripTask: NtripTask): GPSService

fun singlePointRecordingService(gpsService: GPSService): SinglePointRecordingService

fun singlePointRecordingService(gpsService: GPSService, constantHeight: Int): SinglePointRecordingService

fun singlePointRecordingService(gpsService: GPSService, laserService: LaserService, laserConfiguration: LaserConfiguration): SinglePointRecordingService
```
Note: First of all, create ``Authentication`` using the ``init`` method, and then apply the ``check`` method of the ``Authentication`` class to verify the token.

- To get all available versions by ``SoftwareService`` of the software, use the ``getAllAvailableSoftware`` method.

- ``Configuration`` allows the configuration and customization of several VigramSDK components.

- ``BluetoothService``  provides methods to interact with the device at the lower level.

- ``Peripheral`` provides a write and receive interface to communicate with the viDoc device.
### Messages

The messages received from viDoc and related model types.

- ``DeviceMessage``contains messages sent from the viDoc itself.

- ``NMEAMessage`` is an interface containing common properties for NMEA messages (for GGA, GGA, GNS, GST messages)
- ``GGAMessage`` is used for location data and correction information.

- ``GNSMessage`` is used for GNSS fix data.

- ``GSTMessage`` is used for accuracy information.


- ``GSAMessage`` is used for DOP and active satellites information.

- ``Mode`` is a GNSMessage's Mode indicator.

- ``SatelliteMessage`` contains messages about satellite (ublox config, such as ``Dop``, ``Pvt``, ``AcknowledgeMessage``, ``Elevation``, ``NavigationSystemStatus``) sent from the viDoc itself.


- ``DMMCoordinate`` contains the coordinate encoding format used in several NMEA-0183 messages.


- ``Time`` as specified in a NMEA-0183 message.




### Ntrip Connection

How to discover and connect to Ntrip mountpoints.
- ``NtripConnectionInformation`` combines all connection data needed to connect to an NTRIP server besides the necessary mountpoint.

- ``NtripService`` is used to search for NTRIP mountpoints for various NTRIP providers and create ``NtripTask`` instances to access NTRIP data.

- ``NtripMountPoint`` is a NTRIP mountpoint to get RTK information from.

- ``NtripTask`` requests RTK data over a NTRIP connection.


### GPS

How to receive coordinate and accuracy data from viDoc.

- ``GPSService`` to observe location data, location quality characteristics and 
accuracy information.
- ``GPSDisconnectSignal`` is sent by a GPSService when a certain event occurs that should lead to the disconnection of a GPSService.
- ``GPSCoordinate`` containing latitude, longitude, referenceAltitude and geoidSeparation.
- ``GPSServiceQuality`` contains the quality of the currently provided location data.
- ``GPSQualityIndicator`` contains the quality indicator of the GPS position as specified in a GGAMessage.

### Laser

How to access and communicate with the laser module in viDoc.

- Use a ``LaserService`` to start laser distance recording sessions with a given configuration
- ``LaserConfiguration`` is a configuration for the laser module of a viDoc peripheral

### Environment

How to read the device's surroundings and combine with GPS information.

An ``EnvironmentDataService`` contains all the available information from the user device and the viDoc device.


### SinglePoints

How to record single points with increased accuracy.

- ``SinglePoint`` is the result of a single point recording, where a certain position is recorded over a given duration.
- ``SinglePointRecordingService`` can be used to record single points over a given duration. During the recording phase, the service will keep track of all the environment’s data and then merge individual measurements to one combined single point.



### Enumerations
- ``ElevationValue`` contains the values for Filter GNSS Constel lation system currently using in navigation
- ``NavigationSystemType`` сontains enumerations of satellite navigation systems of the GNSS grouping
- ``RateValue`` is used to changing rate of messages value
- ``StateNtrip`` сontains the type of Ntrip connection state log, including connection has been preparing, ready and error in case failure connection.
- ``StateUpdateSoftware`` сontains the type of Software Progress log, including update software has been starting and error in case failure updating software.
