package com.example.esp32ble.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esp32ble.BLEManager
import com.example.esp32ble.BLEViewModel
import com.example.esp32ble.components.ClockCard
import com.example.esp32ble.components.DeviceInfoCard
import com.example.esp32ble.components.RelayStatusCard
import com.example.esp32ble.components.RelayControlCard
@Composable
fun DashboardScreen(
    viewModel: BLEViewModel,
    bleManager: BLEManager
) {

    val info by viewModel.deviceInfo.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {

            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )

        }

        item {

            DeviceInfoCard(
                deviceInfo = info
            )

        }

        item {

            ClockCard(
                rtc = info.rtcTime
            )

        }

        item {

            RelayStatusCard(
                relay1 = info.relay1,
                relay2 = info.relay2
            )
            RelayControlCard(
                bleManager = bleManager,
                relay1Mode = info.relay1Mode,
                relay2Mode = info.relay2Mode
            )
        }

    }

}