package com.example.esp32ble.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esp32ble.BLEManager
import com.example.esp32ble.ScannedDevice

@Composable
fun SettingsScreen(
    bleManager: BLEManager
) {

    var status by remember {
        mutableStateOf("Not Connected")
    }

    val devices = remember {
        mutableStateListOf<ScannedDevice>()
    }

    LaunchedEffect(Unit) {

        bleManager.onDeviceFound = { device ->

            if (devices.none { it.address == device.address }) {
                devices.add(device)
            }

        }

        bleManager.onConnectionChanged = { connected ->

            status = if (connected) {
                "Connected"
            } else {
                "Not Connected"
            }

        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(status)

        Button(
            onClick = {

                devices.clear()

                if (bleManager.isBluetoothEnabled()) {

                    status = "Scanning..."

                    bleManager.startScan()

                } else {

                    status = "Bluetooth OFF"

                }

            }
        ) {
            Text("Scan BLE")
        }

        Button(
            onClick = {

                bleManager.disconnect()

                status = "Disconnected"

            }
        ) {
            Text("Disconnect")
        }

        Text(
            text = "Nearby Devices",
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            items(devices) { device ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {

                            status = "Connecting..."

                            bleManager.connect(device.device)

                        }
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(device.address)

                        Text("RSSI : ${device.rssi}")

                    }

                }

            }

        }

        Text("Firmware Version : V1.0")

        Text("ESP32 Timer Controller")

    }

}