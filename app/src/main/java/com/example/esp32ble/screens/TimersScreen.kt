package com.example.esp32ble.screens
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esp32ble.BLEManager
import com.example.esp32ble.BLEViewModel
import com.example.esp32ble.TimerData
import com.example.esp32ble.components.TimerCard
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimersScreen(
    viewModel: BLEViewModel,
    bleManager: BLEManager
) {

    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var editingTimer by remember {
        mutableStateOf<TimerData?>(null)
    }

    var addingTimer by remember {
        mutableStateOf(false)
    }
    var deletingTimer by remember {
        mutableStateOf<TimerData?>(null)
    }
    val activeTimers = deviceInfo.timers.filter {
        it.name.isNotBlank() &&
                it.name != "EMPTY"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "Timers",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "${activeTimers.size} Active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                }

                FilledTonalButton(
                    onClick = {
                        addingTimer = true
                    }
                ) {
                    Text("+ Add")
                }

            }

        }

        if (activeTimers.isEmpty()) {

            item {

                Card {

                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {

                        Text(
                            text = "No Timers",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Tap the + Add button to create your first timer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }

                }

            }

        }



        items(activeTimers) { timer ->

            TimerCard(
                timer = timer,
                onEdit = {
                    editingTimer = it
                },
                onDelete = {
                    deletingTimer = it
                }
            )

        }

    }

    // Edit existing timer
    editingTimer?.let { timer ->

        ModalBottomSheet(
            onDismissRequest = {
                editingTimer = null
            },
            sheetState = sheetState
        ) {

            TimerEditScreen(
                timer = timer,
                onSave = {

                    bleManager.sendTimer(it)
                    editingTimer = null

                },
                onCancel = {
                    editingTimer = null
                }
            )

        }

    }

    // Add new timer
    if (addingTimer) {

        val emptyTimer = deviceInfo.timers.firstOrNull {
            it.name == "EMPTY"
        }

        if (emptyTimer != null) {

            ModalBottomSheet(
                onDismissRequest = {
                    addingTimer = false
                },
                sheetState = sheetState
            ) {

                TimerEditScreen(
                    timer = emptyTimer,

                    onSave = {

                        bleManager.sendTimer(it)
                        addingTimer = false

                    },

                    onCancel = {
                        addingTimer = false
                    }

                )

            }

        } else {

            AlertDialog(
                onDismissRequest = {
                    addingTimer = false
                },
                confirmButton = {
                    Button(
                        onClick = {
                            addingTimer = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {},
                text = {
                    Text("Maximum number of timers reached.")
                }
            )

        }

    }

    deletingTimer?.let { timer ->

        AlertDialog(
            onDismissRequest = {
                deletingTimer = null
            },

            title = {
                Text(
                    "Delete Timer",
                    style = MaterialTheme.typography.headlineSmall
                )
            },

            text = {
                Text(
                    "This action cannot be undone.\n\n${timer.name}"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        timer.enable = false
                        timer.name = "EMPTY"

                        timer.onHour = 0
                        timer.onMinute = 0
                        timer.offHour = 0
                        timer.offMinute = 0

                        timer.days = 0
                        timer.relay = 0

                        bleManager.sendTimer(timer)

                        deletingTimer = null
                    }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }

            },

            dismissButton = {

                Button(
                    onClick = {
                        deletingTimer = null
                    }
                ) {
                    Text("Cancel")
                }

            }

        )

    }

}