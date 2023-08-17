# ``VigramSDK``

##
![VigramSDKLogoDocumentation](https://vigram.com/wp-content/uploads/2021/04/vigram_smart_documentation_compressed_black.svg)



## What is the new on 1.2.0-beta2.1


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
