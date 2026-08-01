package com.example.esp32ble

data class TimerData(
    var index: Int = 0,
    var name: String = "",
    var enable: Boolean = false,
    var relay: Int = 0,
    var onHour: Int = 0,
    var onMinute: Int = 0,
    var offHour: Int = 0,
    var offMinute: Int = 0,
    var days: Int = 0
)