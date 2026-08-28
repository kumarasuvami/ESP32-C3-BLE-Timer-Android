package com.example.esp32ble

data class DeviceInfo(

    var deviceName: String = "",

    var firmware: String = "",

    var rtcTime: String = "",

    // Relay Status
    var relay1: Boolean = false,
    var relay2: Boolean = false,

    // Relay Mode
    // 0 = AUTO
    // 1 = FORCE ON
    // 2 = FORCE OFF
    var relay1Mode: Int = 0,
    var relay2Mode: Int = 0,

    // Timers
    var timers: MutableList<TimerData> =
        MutableList(10) { index ->

            TimerData(
                index = index,
                name = "EMPTY",

                enable = false,

                onHour = 0,
                onMinute = 0,

                offHour = 0,
                offMinute = 0,

                days = 0,

                relay = 0,

                cyclic = false,

                cycleOnMinutes = 0,
                cycleOffMinutes = 0
            )
        }
)