package com.example.esp32ble.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esp32ble.BLEViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.esp32ble.BLEManager
import com.example.esp32ble.ScannedDevice

@Composable
fun MainScreen() {
    val bleViewModel: BLEViewModel = viewModel()
    val context = LocalContext.current

    val bleManager = remember {
        BLEManager(
            context,
            bleViewModel
        )
    }
    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    Scaffold(

        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Home"
                        )
                    },
                    label = {
                        Text("Home")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Timers"
                        )
                    },
                    label = {
                        Text("Timers")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "RTC"
                        )
                    },
                    label = {
                        Text("RTC")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = {
                        Text("Settings")
                    }
                )

            }

        }

    ) { padding ->

        Surface(
            modifier = Modifier.padding(padding)
        ) {

            when (selectedTab) {

                0 -> DashboardScreen(
                    viewModel = bleViewModel,
                    bleManager = bleManager
                )

                1 -> TimersScreen(
                    viewModel = bleViewModel,
                    bleManager = bleManager
                )

                2 -> RTCScreen(
                    viewModel = bleViewModel,
                    bleManager = bleManager
                )

                3 -> SettingsScreen(
                    bleManager = bleManager,
                    viewModel = bleViewModel
                )

            }

        }

    }

}

@Composable
fun TimersScreenDummy() {
    Text("Timers Screen")
}

@Composable
fun RTCScreenDummy() {
    Text("RTC Screen")
}

@Composable
fun SettingsScreenDummy() {
    Text("Settings Screen")
}