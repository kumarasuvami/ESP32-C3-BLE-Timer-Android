package com.example.esp32ble.screens

// Ready-to-use RTC screen template.
// Replace your current RTCScreen.kt with this file if desired.

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.esp32ble.BLEManager
import com.example.esp32ble.BLEViewModel
import kotlinx.coroutines.launch

@Composable
fun RTCScreen(viewModel: BLEViewModel, bleManager: BLEManager) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var year by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var minute by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }

    fun loadRtc() {
        val p = deviceInfo.rtcTime.trim().split(Regex("\\s+"))
        if (p.size != 2) return
        val d = p[0].split("-")
        val t = p[1].split(":")
        if (d.size != 3 || t.size != 3) return
        year=d[2]; month=d[1]; day=d[0]
        hour=t[0]; minute=t[1]; second=t[2]
    }

    LaunchedEffect(deviceInfo.rtcTime) { if (year.isEmpty()) loadRtc() }

    LaunchedEffect(Unit) {
        bleManager.onRtcUpdated = {
            scope.launch { snackbar.showSnackbar("RTC Updated") }
        }
    }

    Scaffold(snackbarHost={ SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {

            Text("Real Time Clock", style=MaterialTheme.typography.headlineMedium)

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Current RTC")
                    Text(deviceInfo.rtcTime)
                }
            }

            FilledTonalButton(
                onClick = { loadRtc() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download,null)
                Spacer(Modifier.width(8.dp))
                Text("LOAD FROM RTC")
            }

            @Composable
            fun NumField(v:String,l:String,c:(String)->Unit,m:Modifier){
                OutlinedTextField(
                    value=v,
                    onValueChange=c,
                    label={ Text(l) },
                    singleLine=true,
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                    modifier=m
                )
            }

            Text("Date")
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                NumField(year,"Year",{year=it},Modifier.weight(1f))
                NumField(month,"Month",{month=it},Modifier.weight(1f))
                NumField(day,"Day",{day=it},Modifier.weight(1f))
            }

            Text("Time")
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                NumField(hour,"Hour",{hour=it},Modifier.weight(1f))
                NumField(minute,"Minute",{minute=it},Modifier.weight(1f))
                NumField(second,"Second",{second=it},Modifier.weight(1f))
            }

            FilledTonalButton(
                onClick = { bleManager.write("GET_TIME") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh,null)
                Spacer(Modifier.width(8.dp))
                Text("READ RTC")
            }

            Button(
                onClick = {

                    val y = year.toIntOrNull() ?: 0
                    val m = month.toIntOrNull() ?: 0
                    val d = day.toIntOrNull() ?: 0
                    val h = hour.toIntOrNull() ?: 0
                    val min = minute.toIntOrNull() ?: 0
                    val s = second.toIntOrNull() ?: 0

                    if (y < 2000 ||
                        m !in 1..12 ||
                        d !in 1..31 ||
                        h !in 0..23 ||
                        min !in 0..59 ||
                        s !in 0..59
                    ) {
                        scope.launch {
                            snackbar.showSnackbar("Invalid Date / Time")
                        }
                        return@Button
                    }

                    bleManager.write(
                        "SET_TIME,$year,$month,$day,$hour,$minute,$second"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("UPDATE RTC")
            }
        }
    }
}
