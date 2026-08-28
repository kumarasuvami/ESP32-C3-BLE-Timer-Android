package com.example.esp32ble

data class TimerData(

    var index: Int = 0,

    var name: String = "EMPTY",

    var enable: Boolean = false,

    var onHour: Int = 0,
    var onMinute: Int = 0,

    var offHour: Int = 0,
    var offMinute: Int = 0,

    var days: Int = 0,

    // 0 = Relay 1
    // 1 = Relay 2
    // 2 = Both
    var relay: Int = 0,

    // Cyclic timer
    var cyclic: Boolean = false,

    // Cyclic ON duration in minutes
    var cycleOnMinutes: Int = 0,

    // Cyclic OFF duration in minutes
    var cycleOffMinutes: Int = 0,

    // Event triggered timer
    // false = normal scheduled cyclic operation
    // true  = GPIO event starts the cyclic operation
    var eventTriggered: Boolean = false
)