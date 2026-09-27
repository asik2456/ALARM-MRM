package com.example.ui.screens.alarm_edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmEntity
import com.example.data.model.MathDifficulty
import com.example.data.model.WakeMissionType
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.NeonOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditSheet(
    alarm: AlarmEntity?,
    onSave: (AlarmEntity) -> Unit,
    onTestAlarm: (AlarmEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form state initialized from existing alarm or defaults
    var hour by remember { mutableIntStateOf(alarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(alarm?.minute ?: 0) }
    var label by remember { mutableStateOf(alarm?.label ?: "Wake Up!") }
    var repeatDays by remember { mutableStateOf(alarm?.repeatDays ?: "2,3,4,5,6") }
    var ringtoneTitle by remember { mutableStateOf(alarm?.ringtoneTitle ?: "Energetic Pulse") }
    var ringtoneUri by remember { mutableStateOf(alarm?.ringtoneUri ?: "preset_pulse") }
    var volume by remember { mutableFloatStateOf(alarm?.volume ?: 0.9f) }
    var isGentleWakeUp by remember { mutableStateOf(alarm?.isGentleWakeUp ?: true) }
    var isVibrationEnabled by remember { mutableStateOf(alarm?.isVibrationEnabled ?: true) }
    var isSnoozeEnabled by remember { mutableStateOf(alarm?.isSnoozeEnabled ?: true) }
    var snoozeMinutes by remember { mutableIntStateOf(alarm?.snoozeMinutes ?: 5) }
    var missionType by remember { mutableStateOf(alarm?.missionType ?: WakeMissionType.MATH) }
    var mathDifficulty by remember { mutableStateOf(alarm?.mathDifficulty ?: MathDifficulty.MEDIUM) }
    var mathProblemCount by remember { mutableIntStateOf(alarm?.mathProblemCount ?: 3) }

    var showRingtonePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (alarm == null) "Set New Alarm" else "Edit Alarm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Picker Component (Large intuitive digital selector)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Alarm Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Hour Selector
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "▲",
                                modifier = Modifier
                                    .clickable { hour = (hour + 1) % 24 }
                                    .padding(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                            Text(
                                text = String.format("%02d", hour),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("alarm_hour_text")
                            )
                            Text(
                                text = "▼",
                                modifier = Modifier
                                    .clickable { hour = if (hour == 0) 23 else hour - 1 }
                                    .padding(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }

                        Text(
                            text = ":",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        // Minute Selector
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "▲",
                                modifier = Modifier
                                    .clickable { minute = (minute + 1) % 60 }
                                    .padding(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                            Text(
                                text = String.format("%02d", minute),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("alarm_minute_text")
                            )
                            Text(
                                text = "▼",
                                modifier = Modifier
                                    .clickable { minute = if (minute == 0) 59 else minute - 1 }
                                    .padding(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Quick +/- 5 or 15 minutes
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = false,
                                onClick = { minute = (minute + 5) % 60 },
                                label = { Text("+5m") },
                                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface)
                            )
                            FilterChip(
                                selected = false,
                                onClick = { minute = (minute + 15) % 60 },
                                label = { Text("+15m") },
                                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alarm Label
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Alarm Label") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alarm_label_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat Days Selector
            Text(
                text = "Repeat Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            val daysList = if (repeatDays.isBlank()) emptyList() else repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }
            val weekDayMap = listOf(
                2 to "M", 3 to "T", 4 to "W", 5 to "T", 6 to "F", 7 to "S", 1 to "S"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDayMap.forEach { (dayInt, labelText) ->
                    val isSelected = daysList.contains(dayInt)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                val updated = if (isSelected) {
                                    daysList.filter { it != dayInt }
                                } else {
                                    daysList + dayInt
                                }
                                repeatDays = updated.sorted().joinToString(",")
                            }
                            .testTag("day_button_$dayInt"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelText,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick day presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = repeatDays == "2,3,4,5,6",
                    onClick = { repeatDays = "2,3,4,5,6" },
                    label = { Text("Weekdays") }
                )
                FilterChip(
                    selected = repeatDays == "1,7",
                    onClick = { repeatDays = "1,7" },
                    label = { Text("Weekends") }
                )
                FilterChip(
                    selected = repeatDays == "1,2,3,4,5,6,7",
                    onClick = { repeatDays = "1,2,3,4,5,6,7" },
                    label = { Text("Everyday") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Wake-up Mission Section (Alarmy Signature Feature!)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = NeonOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wake-Up Mission",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Alarmy Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonOrange,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .background(NeonOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "Forces you to wake up by solving challenges before the alarm stops ringing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Mission Type selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = missionType == WakeMissionType.MATH,
                    onClick = { missionType = WakeMissionType.MATH },
                    label = { Text("Math Challenge") },
                    leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null, Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("mission_math_chip")
                )
                FilterChip(
                    selected = missionType == WakeMissionType.SHAKE,
                    onClick = { missionType = WakeMissionType.SHAKE },
                    label = { Text("Shake Phone") },
                    leadingIcon = { Icon(Icons.Default.Vibration, contentDescription = null, Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("mission_shake_chip")
                )
            }

            if (missionType == WakeMissionType.MATH) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Math Difficulty:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MathDifficulty.values().forEach { diff ->
                                val isSelected = mathDifficulty == diff
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable { mathDifficulty = diff }
                                        .padding(vertical = 8.dp)
                                        .testTag("diff_${diff.name}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = diff.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Text(
                            text = mathDifficulty.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Number of problems
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Number of Problems to Solve:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$mathProblemCount questions",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1, 2, 3, 4, 5).forEach { count ->
                                FilterChip(
                                    selected = mathProblemCount == count,
                                    onClick = { mathProblemCount = count },
                                    label = { Text("$count") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Ringtone & Audio Section
            Text(
                text = "Alarm Sound & Volume",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Ringtone selector card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRingtonePicker = true }
                    .testTag("ringtone_picker_trigger"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = CyberBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ringtone Sound",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = ringtoneTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        text = "Change",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Volume Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Volume", style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = "${(volume * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = volume,
                onValueChange = { volume = it },
                valueRange = 0.1f..1.0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alarm_volume_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            // Gentle Wake-up toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Gentle Wake-Up",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Ramps up volume gradually over 25 seconds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isGentleWakeUp,
                    onCheckedChange = { isGentleWakeUp = it },
                    modifier = Modifier.testTag("gentle_wakeup_switch")
                )
            }

            // Vibration toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vibration",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Vibrate phone in sync with alarm rhythm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isVibrationEnabled,
                    onCheckedChange = { isVibrationEnabled = it },
                    modifier = Modifier.testTag("vibration_switch")
                )
            }

            // Snooze toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Allow Snooze",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Snooze for $snoozeMinutes minutes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isSnoozeEnabled,
                    onCheckedChange = { isSnoozeEnabled = it },
                    modifier = Modifier.testTag("snooze_switch")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Test Alarm & Save
            val currentConfig = AlarmEntity(
                id = alarm?.id ?: 0,
                hour = hour,
                minute = minute,
                label = label.ifBlank { "Wake Up!" },
                isEnabled = true,
                repeatDays = repeatDays,
                ringtoneTitle = ringtoneTitle,
                ringtoneUri = ringtoneUri,
                volume = volume,
                isGentleWakeUp = isGentleWakeUp,
                isVibrationEnabled = isVibrationEnabled,
                isSnoozeEnabled = isSnoozeEnabled,
                snoozeMinutes = snoozeMinutes,
                missionType = missionType,
                mathDifficulty = mathDifficulty,
                mathProblemCount = mathProblemCount
            )

            // Test Alarm Now Button (Crucial feature so user can immediately test math puzzles & sound)
            OutlinedButton(
                onClick = {
                    onTestAlarm(currentConfig)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("test_alarm_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeonOrange
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Test Alarm & Math Challenge Now",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onSave(currentConfig)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_alarm_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (alarm == null) "Create Alarm" else "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showRingtonePicker) {
        RingtoneSelectorDialog(
            currentRingtoneUri = ringtoneUri,
            currentVolume = volume,
            onRingtoneSelected = { title, uri ->
                ringtoneTitle = title
                ringtoneUri = uri
            },
            onDismiss = { showRingtonePicker = false }
        )
    }
}
