package com.example.esp32ble.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esp32ble.TimerData

private object EditScreenColors {
    val SurfaceTop = Color(0xFF1C2027)
    val SurfaceBottom = Color(0xFF15181D)
    val SurfaceElevated = Color(0xFF20242B)
    val Field = Color(0xFF1A1E24)

    val TextPrimary = Color(0xFFF0F2F5)
    val TextSecondary = Color(0xFF9AA0AA)
    val TextMuted = Color(0xFF62676F)

    val Accent = Color(0xFF3DD6C4)
    val AccentSoft = Color(0xFF213C3A)

    val Danger = Color(0xFFF16A70)
}

private val MonoFamily = FontFamily.Monospace
private val PanelShape = RoundedCornerShape(24.dp)
private val FieldShape = RoundedCornerShape(14.dp)
private val PillShape = RoundedCornerShape(50)

@Composable
fun TimerEditScreen(
    timer: TimerData,
    onSave: (TimerData) -> Unit,
    onCancel: () -> Unit
) {
    val colors = EditScreenColors

    var name by remember { mutableStateOf(timer.name) }
    var enable by remember { mutableStateOf(timer.enable) }

    var onHour by remember { mutableStateOf(timer.onHour.toString()) }
    var onMinute by remember { mutableStateOf(timer.onMinute.toString()) }

    var offHour by remember { mutableStateOf(timer.offHour.toString()) }
    var offMinute by remember { mutableStateOf(timer.offMinute.toString()) }

    var relay by remember { mutableStateOf(timer.relay) }
    var days by remember { mutableStateOf(timer.days) }

    // ----------------------------------------------------
    // Cyclic timer
    // ----------------------------------------------------

    var cyclic by remember { mutableStateOf(timer.cyclic) }

    var eventTriggered by remember {
        mutableStateOf(timer.eventTriggered)
    }

    var cycleOnMinutes by remember {
        mutableStateOf(timer.cycleOnMinutes.toString())
    }

    var cycleOffMinutes by remember {
        mutableStateOf(timer.cycleOffMinutes.toString())
    }

    val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.Accent,
        unfocusedBorderColor = Color.Transparent,
        focusedLabelColor = colors.Accent,
        unfocusedLabelColor = colors.TextSecondary,
        focusedTextColor = colors.TextPrimary,
        unfocusedTextColor = colors.TextPrimary,
        cursorColor = colors.Accent,
        focusedContainerColor = colors.Field,
        unfocusedContainerColor = colors.Field
    )

    val monoFieldStyle =
        TextStyle(
            fontFamily = MonoFamily,
            fontSize = 16.sp
        )

    Surface(
        shape = PanelShape,
        color = Color.Transparent,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.SurfaceTop,
                            colors.SurfaceBottom
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(24.dp),

            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            //----------------------------------------------------
            // Header
            //----------------------------------------------------

            Column {

                Text(
                    text = "TMR-${(timer.index + 1).toString().padStart(2, '0')}",
                    color = colors.TextMuted,
                    fontFamily = MonoFamily,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Edit Timer",
                    color = colors.TextPrimary,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            //----------------------------------------------------
            // Timer name
            //----------------------------------------------------

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text("Timer name")
                },
                singleLine = true,
                colors = fieldColors,
                shape = FieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            //----------------------------------------------------
            // Enable
            //----------------------------------------------------

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FieldShape)
                    .background(colors.Field)
                    .padding(
                        horizontal = 18.dp,
                        vertical = 14.dp
                    ),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    "Enabled",
                    color = colors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Switch(
                    checked = enable,
                    onCheckedChange = {
                        enable = it
                    },

                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF08211D),
                        checkedTrackColor = colors.Accent,
                        uncheckedThumbColor = colors.TextMuted,
                        uncheckedTrackColor = colors.SurfaceElevated,
                        uncheckedBorderColor = Color.Transparent,
                        checkedBorderColor = Color.Transparent
                    )
                )
            }

            //----------------------------------------------------
            // Schedule
            //----------------------------------------------------

            SectionLabel("Schedule")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FieldShape)
                    .background(colors.Field)
                    .padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    FieldGroupLabel("ON TIME")

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        TimeField(
                            onHour,
                            { onHour = it },
                            "HH",
                            colors,
                            monoFieldStyle,
                            Modifier.weight(1f)
                        )

                        TimeField(
                            onMinute,
                            { onMinute = it },
                            "MM",
                            colors,
                            monoFieldStyle,
                            Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(
                    color = colors.SurfaceElevated,
                    thickness = 1.dp
                )

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    FieldGroupLabel("OFF TIME")

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        TimeField(
                            offHour,
                            { offHour = it },
                            "HH",
                            colors,
                            monoFieldStyle,
                            Modifier.weight(1f)
                        )

                        TimeField(
                            offMinute,
                            { offMinute = it },
                            "MM",
                            colors,
                            monoFieldStyle,
                            Modifier.weight(1f)
                        )
                    }
                }
            }

            //----------------------------------------------------
            // Cyclic Timer
            //----------------------------------------------------

            SectionLabel("Cyclic Timer")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FieldShape)
                    .background(colors.Field)
                    .padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column {

                        Text(
                            text = "Cyclic operation",
                            color = colors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text =
                                "Repeat ON / OFF inside the schedule",
                            color = colors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = cyclic,
                        onCheckedChange = {
                            cyclic = it
                        },

                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF08211D),
                            checkedTrackColor = colors.Accent,
                            uncheckedThumbColor = colors.TextMuted,
                            uncheckedTrackColor = colors.SurfaceElevated,
                            uncheckedBorderColor = Color.Transparent,
                            checkedBorderColor = Color.Transparent
                        )
                    )
                }

                //------------------------------------------------
                // Show cycle settings only when enabled
                //------------------------------------------------

                if (cyclic) {

                    HorizontalDivider(
                        color = colors.SurfaceElevated,
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        OutlinedTextField(
                            value = cycleOnMinutes,

                            onValueChange = {
                                if (
                                    it.all { char ->
                                        char.isDigit()
                                    }
                                ) {
                                    cycleOnMinutes = it
                                }
                            },

                            label = {
                                Text(
                                    "ON minutes",
                                    fontFamily = MonoFamily,
                                    fontSize = 11.sp
                                )
                            },

                            singleLine = true,

                            colors = fieldColors,

                            shape = FieldShape,

                            textStyle = monoFieldStyle,

                            modifier =
                                Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = cycleOffMinutes,

                            onValueChange = {
                                if (
                                    it.all { char ->
                                        char.isDigit()
                                    }
                                ) {
                                    cycleOffMinutes = it
                                }
                            },

                            label = {
                                Text(
                                    "OFF minutes",
                                    fontFamily = MonoFamily,
                                    fontSize = 11.sp
                                )
                            },

                            singleLine = true,

                            colors = fieldColors,

                            shape = FieldShape,

                            textStyle = monoFieldStyle,

                            modifier =
                                Modifier.weight(1f)
                        )
                    }

                    //------------------------------------------------
                    // Event Triggered
                    //------------------------------------------------

                    HorizontalDivider(
                        color = colors.SurfaceElevated,
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                text = "Event triggered",
                                color = colors.TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Start cycle when GPIO 5 receives LOW",
                                color = colors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = eventTriggered,
                            onCheckedChange = {
                                eventTriggered = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF08211D),
                                checkedTrackColor = colors.Accent,
                                uncheckedThumbColor = colors.TextMuted,
                                uncheckedTrackColor = colors.SurfaceElevated,
                                uncheckedBorderColor = Color.Transparent,
                                checkedBorderColor = Color.Transparent
                            )
                        )
                    }



                }
            }

            //----------------------------------------------------
            // Relay
            //----------------------------------------------------

            SectionLabel("Relay channel")

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                RelayOption(
                    "Relay 1",
                    selected = relay == 0,
                    colors = colors
                ) {
                    relay = 0
                }

                RelayOption(
                    "Relay 2",
                    selected = relay == 1,
                    colors = colors
                ) {
                    relay = 1
                }

                RelayOption(
                    "Both",
                    selected = relay == 2,
                    colors = colors
                ) {
                    relay = 2
                }
            }

            //----------------------------------------------------
            // Days
            //----------------------------------------------------

            SectionLabel("Active days")

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),

                modifier =
                    Modifier.height(44.dp),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(dayNames.indices.toList()) { index ->

                    val selected =
                        (days and (1 shl index)) != 0

                    DayOption(
                        label = dayNames[index],
                        selected = selected,
                        colors = colors,

                        onClick = {

                            days =
                                if (selected) {

                                    days and
                                            (1 shl index).inv()

                                } else {

                                    days or
                                            (1 shl index)
                                }
                        }
                    )
                }
            }

            Spacer(
                Modifier.height(4.dp)
            )

            //----------------------------------------------------
            // Actions
            //----------------------------------------------------

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                OutlinedButton(
                    modifier =
                        Modifier.weight(1f),

                    onClick = onCancel,

                    shape = PillShape,

                    border =
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            colors.SurfaceElevated
                        ),

                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor =
                                colors.TextSecondary
                        ),

                    contentPadding =
                        PaddingValues(vertical = 13.dp)
                ) {

                    Text(
                        "Cancel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                //------------------------------------------------
                // SAVE
                //------------------------------------------------

                Button(
                    modifier =
                        Modifier.weight(1f),

                    shape = PillShape,

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                colors.Accent,

                            contentColor =
                                Color(0xFF08211D)
                        ),

                    elevation =
                        ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 1.dp
                        ),

                    contentPadding =
                        PaddingValues(vertical = 13.dp),

                    onClick = {

                        //------------------------------------------------
                        // IMPORTANT:
                        // TimerData uses val properties.
                        // Therefore use copy(), NOT timer.name = ...
                        //------------------------------------------------

                        val updatedTimer =
                            timer.copy(

                                name = name,

                                enable = enable,

                                onHour =
                                    onHour.toIntOrNull()
                                        ?: 0,

                                onMinute =
                                    onMinute.toIntOrNull()
                                        ?: 0,

                                offHour =
                                    offHour.toIntOrNull()
                                        ?: 0,

                                offMinute =
                                    offMinute.toIntOrNull()
                                        ?: 0,

                                relay = relay,

                                days = days,

                                cyclic = cyclic,

                                cycleOnMinutes =
                                    cycleOnMinutes
                                        .toIntOrNull()
                                        ?: 0,

                                cycleOffMinutes =
                                    cycleOffMinutes
                                        .toIntOrNull()
                                        ?: 0,

                                eventTriggered = eventTriggered
                            )

                        onSave(updatedTimer)
                    }
                ) {

                    Text(
                        "Save",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(
    text: String
) {
    Text(
        text = text,
        color = EditScreenColors.TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.3.sp
    )
}

@Composable
private fun FieldGroupLabel(
    text: String
) {
    Text(
        text = text,
        color = EditScreenColors.TextMuted,
        fontFamily = MonoFamily,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    )
}

@Composable
private fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    colors: EditScreenColors,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,

        label = {
            Text(
                label,
                fontFamily = MonoFamily,
                fontSize = 11.sp
            )
        },

        singleLine = true,

        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor =
                    colors.Accent,

                unfocusedBorderColor =
                    colors.SurfaceElevated,

                focusedLabelColor =
                    colors.Accent,

                unfocusedLabelColor =
                    colors.TextMuted,

                focusedTextColor =
                    colors.TextPrimary,

                unfocusedTextColor =
                    colors.TextPrimary,

                cursorColor =
                    colors.Accent,

                focusedContainerColor =
                    colors.SurfaceTop,

                unfocusedContainerColor =
                    colors.SurfaceTop
            ),

        shape =
            RoundedCornerShape(12.dp),

        textStyle = textStyle,

        modifier = modifier
    )
}

@Composable
private fun RelayOption(
    label: String,
    selected: Boolean,
    colors: EditScreenColors,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,

        onClick = onClick,

        label = {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        },

        shape = PillShape,

        colors =
            FilterChipDefaults.filterChipColors(
                containerColor = colors.Field,
                labelColor = colors.TextSecondary,
                selectedContainerColor = colors.Accent,
                selectedLabelColor = Color(0xFF08211D)
            ),

        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = Color.Transparent,
                selectedBorderColor = Color.Transparent
            )
    )
}

@Composable
private fun DayOption(
    label: String,
    selected: Boolean,
    colors: EditScreenColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(
                if (selected)
                    colors.Accent
                else
                    colors.Field
            ),

        contentAlignment =
            Alignment.Center
    ) {

        androidx.compose.material3.IconButton(
            onClick = onClick,
            modifier = Modifier.matchParentSize()
        ) {

            Text(
                text = label,

                color =
                    if (selected)
                        Color(0xFF08211D)
                    else
                        colors.TextMuted,

                fontSize = 13.sp,

                fontWeight =
                    if (selected)
                        FontWeight.Bold
                    else
                        FontWeight.Normal
            )
        }
    }
}