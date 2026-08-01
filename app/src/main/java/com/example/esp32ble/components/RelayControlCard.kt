package com.example.esp32ble.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esp32ble.BLEManager

@Composable
private fun ModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick
        ) {
            Text(text)
        }
    } else {
        FilledTonalButton(
            onClick = onClick
        ) {
            Text(text)
        }
    }
}

@Composable
fun RelayControlCard(
    bleManager: BLEManager,
    relay1Mode: Int,
    relay2Mode: Int
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Relay 1",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                ModeButton(
                    text = "AUTO",
                    selected = relay1Mode == 0
                ) {
                    bleManager.setRelayMode(0, 0)
                }

                ModeButton(
                    text = "ON",
                    selected = relay1Mode == 1
                ) {
                    bleManager.setRelayMode(0, 1)
                }

                ModeButton(
                    text = "OFF",
                    selected = relay1Mode == 2
                ) {
                    bleManager.setRelayMode(0, 2)
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Relay 2",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                ModeButton(
                    text = "AUTO",
                    selected = relay2Mode == 0
                ) {
                    bleManager.setRelayMode(1, 0)
                }

                ModeButton(
                    text = "ON",
                    selected = relay2Mode == 1
                ) {
                    bleManager.setRelayMode(1, 1)
                }

                ModeButton(
                    text = "OFF",
                    selected = relay2Mode == 2
                ) {
                    bleManager.setRelayMode(1, 2)
                }

            }

        }

    }

}