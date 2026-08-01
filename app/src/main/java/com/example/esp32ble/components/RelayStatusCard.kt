package com.example.esp32ble.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RelayStatusCard(
    relay1: Boolean,
    relay2: Boolean
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Power,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    "Relay Status",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            RelayRow(
                relayName = "Relay 1",
                state = relay1
            )

            Spacer(modifier = Modifier.height(12.dp))

            RelayRow(
                relayName = "Relay 2",
                state = relay2
            )

        }
    }
}

@Composable
private fun RelayRow(
    relayName: String,
    state: Boolean
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            relayName,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            if (state) "🟢 ON" else "🔴 OFF",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}