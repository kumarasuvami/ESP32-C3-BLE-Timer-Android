package com.example.esp32ble.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.esp32ble.DeviceInfo

@Composable
fun DeviceInfoCard(
    deviceInfo: DeviceInfo
) {

    // Replace with your actual BLE connection variable later
    val connected = true

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Device Information",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            if (connected)
                                "Connected"
                            else
                                "Disconnected"
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor =
                            if (connected)
                                Color(0xFFDFF5E1)
                            else
                                Color(0xFFFFE0E0),

                        disabledLabelColor =
                            if (connected)
                                Color(0xFF2E7D32)
                            else
                                Color(0xFFC62828)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                icon = {
                    Icon(
                        Icons.Default.Memory,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = "Device",
                value = deviceInfo.deviceName
            )

            Spacer(modifier = Modifier.height(14.dp))

            InfoRow(
                icon = {
                    Icon(
                        Icons.Default.Build,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = "Firmware",
                value = deviceInfo.firmware
            )

            Spacer(modifier = Modifier.height(14.dp))

            InfoRow(
                icon = {
                    Icon(
                        Icons.Default.Bluetooth,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = "Bluetooth",
                value = if (connected) "Connected" else "Disconnected"
            )

            Spacer(modifier = Modifier.height(14.dp))

            InfoRow(
                icon = {
                    Icon(
                        Icons.Default.Schedule,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = "RTC Module",
                value = "DS3231 Ready"
            )

        }
    }
}

@Composable
private fun InfoRow(
    icon: @Composable () -> Unit,
    title: String,
    value: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        icon()

        Spacer(modifier = Modifier.width(12.dp))

        Column {

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )

        }

    }

}