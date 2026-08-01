package com.example.esp32ble.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esp32ble.BLEManager
import com.example.esp32ble.BLEViewModel
import com.example.esp32ble.ScannedDevice
import com.example.esp32ble.components.DeviceInfoCard

@Composable
fun BLEScreen(
    viewModel: BLEViewModel,
    bleManager: BLEManager
) {

    var status by remember {
        mutableStateOf("Not Connected")
    }

    val devices = remember {
        mutableStateListOf<ScannedDevice>()
    }





// Observe data coming from BLEManager
    val deviceInfo by viewModel.deviceInfo.collectAsState()



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                text = "ESP32 Timer Controller",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text(text = status)
        }

        item {
            DeviceInfoCard(deviceInfo)
        }






    }




}