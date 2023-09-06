# ``VigramSDK``

##

## What is the new on 1.2.3

### GPSService:
Fixed the functioning of the ``timestampedCoordinate`` method of the ``GPSService`` interface. The signature of the ``timestampedCoordinate`` method has been changed:
```kotlin
fun timestampedCoordinate(callback: Callback2<Date?, GPSCoordinate>)
```
Usage example:

```kotlin
timestampedCoordinate { date, gpsCoordinate ->
    Log.e("GPSService", "gpsCoordinate=$gpsCoordinate")
    date?.let { date ->
        Log.e("GPSService", "date=$date")
    }
}

```
### Peripheral:
Fixed a bug with receiving an Ack Message after changing the status of a constellation.

Usage example:
```kotlin
peripheral.changeStatusGNSS(NavigationSystemType.GALILEO, true) {
    Log.e("ACK MESSAGE", "${it.getResult()}")
}
```