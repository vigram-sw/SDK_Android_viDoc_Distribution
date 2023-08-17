# ``VigramSDK``

##

## What is the new on 1.2.1-beta2.2

### NtripTask:
Fixed the functioning of the ``data`` method. The signature of the ``data`` method has been changed:
```kotlin
fun data(callback: Callback1<ByteArray>)
```
Usage example:

```kotlin
ntripTask.data {
	Log.i("NTRIP", "received bytes: $it}")
}
```