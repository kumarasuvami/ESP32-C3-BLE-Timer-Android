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

import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.example.esp32ble.BLEViewModel
@Composable
fun SettingsScreen(
    bleManager: BLEManager,
    viewModel: BLEViewModel
){
    val context = LocalContext.current

    var showLocationDialog by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
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

            if (connected) {
                message = ""
            } else {
                message = "Disconnected"
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

        Text(
            text = if (viewModel.connected)
                "Connected"
            else
                "Not Connected"
        )

        Text(message)

        Button(
            onClick = {

                devices.clear()

                if (!bleManager.isBluetoothEnabled()) {

                    message = "Bluetooth OFF"

                } else {

                    val locationManager =
                        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                    val locationEnabled =
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                    if (!locationEnabled) {

                        message = "Location OFF"
                        showLocationDialog = true

                    } else {

                        message = "Scanning..."
                        bleManager.startScan()

                    }
                }

            }
        ) {
            Text("Scan BLE")
        }

        Button(
            onClick = {

                bleManager.disconnect()



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

                            message = "Connecting..."

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

    if (showLocationDialog) {

        AlertDialog(
            onDismissRequest = {
                showLocationDialog = false
            },
            title = {
                Text("Location Required")
            },
            text = {
                Text("Please turn on Location to scan for BLE devices.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationDialog = false

                        context.startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    }
                ) {
                    Text("Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLocationDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

}