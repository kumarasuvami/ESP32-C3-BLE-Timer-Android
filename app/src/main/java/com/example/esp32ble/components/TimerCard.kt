package com.example.esp32ble.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esp32ble.TimerData

/**
 * Elevated "classic" dark palette — soft depth via shadow/gradient
 * rather than hard borders. Kept local to this file.
 */
private object TimerCardColors {
    val SurfaceTop = Color(0xFF1C2027)
    val SurfaceBottom = Color(0xFF15181D)
    val SurfaceElevated = Color(0xFF20242B)

    val TextPrimary = Color(0xFFF0F2F5)
    val TextSecondary = Color(0xFF9AA0AA)
    val TextMuted = Color(0xFF62676F)

    val Accent = Color(0xFF3DD6C4)
    val AccentSoft = Color(0xFF213C3A)

    val StatusOn = Color(0xFF4ADE80)
    val StatusOnSoft = Color(0xFF1E332A)
    val StatusOff = Color(0xFF6B7078)
    val StatusOffSoft = Color(0xFF23262C)

    val Danger = Color(0xFFF16A70)
}

private val MonoFamily = FontFamily.Monospace
private val CardShape = RoundedCornerShape(22.dp)
private val PillShape = RoundedCornerShape(50)

@Composable
fun TimerCard(
    timer: TimerData,
    onEdit: (TimerData) -> Unit = {},
    onDelete: (TimerData) -> Unit = {}
) {
    val colors = TimerCardColors

    val statusColor by animateColorAsState(
        targetValue = if (timer.enable) colors.StatusOn else colors.StatusOff,
        label = "statusColor"
    )
    val statusSoft = if (timer.enable) colors.StatusOnSoft else colors.StatusOffSoft

    Surface(
        shape = CardShape,
        color = Color.Transparent,
        shadowElevation = 10.dp,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(colors.SurfaceTop, colors.SurfaceBottom))
                )
                .padding(22.dp)
        ) {

            //----------------------------------------------------
            // Header — name, ID, status badge
            //----------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = timer.name.ifBlank { "Untitled Timer" },
                        color = colors.TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "TMR-${(timer.index + 1).toString().padStart(2, '0')}",
                        color = colors.TextMuted,
                        fontFamily = MonoFamily,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                StatusBadge(
                    active = timer.enable,
                    dotColor = statusColor,
                    background = statusSoft
                )
            }

            Spacer(Modifier.height(20.dp))

            //----------------------------------------------------
            // Schedule readout, framed with a soft icon chip
            //----------------------------------------------------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.AccentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = colors.Accent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column {
                    Text(
                        text = "%02d:%02d  –  %02d:%02d".format(
                            timer.onHour, timer.onMinute,
                            timer.offHour, timer.offMinute
                        ),
                        color = colors.TextPrimary,
                        fontFamily = MonoFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "ON  →  OFF",
                        color = colors.TextMuted,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = colors.SurfaceElevated, thickness = 1.dp)
            Spacer(Modifier.height(18.dp))

            //----------------------------------------------------
            // Relay
            //----------------------------------------------------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Bolt,
                    contentDescription = null,
                    tint = colors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Channel",
                    color = colors.TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.weight(1f))
                RelayTag(
                    text = when (timer.relay) {
                        0 -> "Relay 1"
                        1 -> "Relay 2"
                        else -> "Relay 1 + 2"
                    }
                )
            }

            Spacer(Modifier.height(14.dp))

            //----------------------------------------------------
            // Days — circular indicators
            //----------------------------------------------------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = colors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))

                val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")

                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    for (i in 0..6) {
                        val active = (timer.days and (1 shl i)) != 0
                        DayDot(label = dayNames[i], active = active)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            //----------------------------------------------------
            // Actions
            //----------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onDelete(timer) },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.Danger)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Delete", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.width(6.dp))

                Button(
                    onClick = { onEdit(timer) },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.Accent,
                        contentColor = Color(0xFF08211D)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 1.dp
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text("Edit", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Rounded pill status badge with a soft tinted background. */
@Composable
private fun StatusBadge(active: Boolean, dotColor: Color, background: Color) {
    val colors = TimerCardColors

    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(background)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = if (active) "Enabled" else "Disabled",
            color = colors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Small pill tag for the relay channel value. */
@Composable
private fun RelayTag(text: String) {
    val colors = TimerCardColors

    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(colors.SurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = colors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Circular day-of-week indicator. */
@Composable
private fun DayDot(label: String, active: Boolean) {
    val colors = TimerCardColors

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (active) colors.Accent else colors.SurfaceElevated),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) Color(0xFF08211D) else colors.TextMuted,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}