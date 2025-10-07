package com.vigram.sdkgh1.data

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.vigram.sdkgh1.domain.repository.BluetoothRepository
import com.vigram.sdk.Bluetooth.BluetoothService
import com.vigram.sdk.Vigram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BluetoothRepositoryImpl(
    private val scope: CoroutineScope,
) : BluetoothRepository {

    @Volatile
    private var _bluetoothService: BluetoothService? = null

    override val bluetoothService: BluetoothService
        get() = _bluetoothService
            ?: throw IllegalStateException("BluetoothService is not initialized")

    override fun init(context: Context) {
        if (_bluetoothService == null) {
            synchronized(this) {
                if (_bluetoothService == null) {
                    _bluetoothService = Vigram.bluetoothService()

                    bluetoothService.getDevices { devices ->
                        scope.launch {
                            _devices.emit(devices)
                        }
                    }
                }
            }
        }
    }

    override fun startScan() {
        bluetoothService.startScan()
        _isScan.value = true
    }

    override fun stopScan() {
        bluetoothService.stopScan()
        _isScan.value = false
    }

    private val _isScan: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isScan: StateFlow<Boolean> = _isScan.asStateFlow()

    private val _devices: MutableStateFlow<List<BluetoothDevice>> = MutableStateFlow(emptyList())
    override val devices: StateFlow<List<BluetoothDevice>> = _devices.asStateFlow()
}