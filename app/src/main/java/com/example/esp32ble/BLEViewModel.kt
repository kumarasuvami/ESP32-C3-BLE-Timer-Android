package com.example.esp32ble

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BLEViewModel : ViewModel() {

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo

    var connected by mutableStateOf(false)
        private set

    fun updateConnectionState(value: Boolean) {
        connected = value
    }

    fun update(info: DeviceInfo) {
        _deviceInfo.value = info.copy(
            timers = info.timers.map { it.copy() }.toMutableList()
        )
    }
}