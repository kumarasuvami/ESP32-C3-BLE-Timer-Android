package com.example.esp32ble.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ClockCard(
    rtc: String
) {

    var date = "--"
    var time = "--:--:--"

    val parts = rtc.trim().split(Regex("\\s+"))

    if (parts.size == 2) {
        date = parts[0]
        time = parts[1]
    }

    var isAnalog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        "Current Time",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                IconButton(
                    onClick = { isAnalog = !isAnalog }
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Toggle clock style"
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (isAnalog) {

                AnalogClockFace(time = time)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = date,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

            } else {

                Text(
                    text = date,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = time,
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

@Composable
private fun AnalogClockFace(time: String) {

    val timeParts = time.split(":")

    val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
    val second = timeParts.getOrNull(2)?.toIntOrNull() ?: 0

    val faceColor = MaterialTheme.colorScheme.surfaceVariant
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val hourHandColor = MaterialTheme.colorScheme.onSurface
    val minuteHandColor = MaterialTheme.colorScheme.onSurface
    val secondHandColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp)
        ) {

            val radius = min(size.width, size.height) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Face
            drawCircle(
                color = faceColor,
                radius = radius
            )

            // Hour ticks
            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30 - 90).toDouble())
                val outer = Offset(
                    center.x + radius * 0.9f * cos(angle).toFloat(),
                    center.y + radius * 0.9f * sin(angle).toFloat()
                )
                val inner = Offset(
                    center.x + radius * 0.78f * cos(angle).toFloat(),
                    center.y + radius * 0.78f * sin(angle).toFloat()
                )
                drawLine(
                    color = tickColor,
                    start = inner,
                    end = outer,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }

            // Hour hand
            val hourAngle = Math.toRadians(((hour % 12) * 30 + minute * 0.5 - 90).toDouble())
            drawLine(
                color = hourHandColor,
                start = center,
                end = Offset(
                    center.x + radius * 0.5f * cos(hourAngle).toFloat(),
                    center.y + radius * 0.5f * sin(hourAngle).toFloat()
                ),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )

            // Minute hand
            val minuteAngle = Math.toRadians((minute * 6 + second * 0.1 - 90).toDouble())
            drawLine(
                color = minuteHandColor,
                start = center,
                end = Offset(
                    center.x + radius * 0.72f * cos(minuteAngle).toFloat(),
                    center.y + radius * 0.72f * sin(minuteAngle).toFloat()
                ),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            // Second hand
            val secondAngle = Math.toRadians((second * 6 - 90).toDouble())
            drawLine(
                color = secondHandColor,
                start = center,
                end = Offset(
                    center.x + radius * 0.82f * cos(secondAngle).toFloat(),
                    center.y + radius * 0.82f * sin(secondAngle).toFloat()
                ),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )

            // Center pin
            drawCircle(
                color = secondHandColor,
                radius = 6f,
                center = center
            )
        }
    }
}