package com.example.esp32ble

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BLEViewModel : ViewModel() {

    private val _deviceInfo = MutableStateFlow(DeviceInfo())

    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo

    // BLEViewModel.kt
    fun update(info: DeviceInfo) {
        _deviceInfo.value = info.copy(
            timers = info.timers.map { it.copy() }.toMutableList()
        )
    }
}