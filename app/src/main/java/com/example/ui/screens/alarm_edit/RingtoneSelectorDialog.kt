package com.example.ui.screens.alarm_edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sound.SoundManager
import com.example.sound.SoundPreset

@Composable
fun RingtoneSelectorDialog(
    currentRingtoneUri: String,
    currentVolume: Float,
    onRingtoneSelected: (title: String, uri: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf(currentRingtoneUri) }
    var selectedTitle by remember {
        val preset = SoundManager.PRESETS.find { it.id == currentRingtoneUri }
        mutableStateOf(preset?.title ?: "Custom Ringtone")
    }
    var currentlyPlayingUri by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            SoundManager.stopAlarm()
        }
    }

    AlertDialog(
        onDismissRequest = {
            SoundManager.stopAlarm()
            onDismiss()
        },
        title = {
            Text(
                text = "Choose Alarm Sound",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select high-tempo alarm tones designed to break heavy sleep:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    items(SoundManager.PRESETS) { preset ->
                        val isSelected = selectedUri == preset.id
                        val isPlaying = currentlyPlayingUri == preset.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedUri = preset.id
                                    selectedTitle = preset.title
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedUri = preset.id
                                    selectedTitle = preset.title
                                },
                                modifier = Modifier.testTag("radio_ringtone_${preset.id}")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = preset.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledIconButton(
                                onClick = {
                                    if (isPlaying) {
                                        SoundManager.stopAlarm()
                                        currentlyPlayingUri = null
                                    } else {
                                        currentlyPlayingUri = preset.id
                                        SoundManager.previewSound(context, preset.id, currentVolume)
                                    }
                                },
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("preview_sound_${preset.id}"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Stop Preview" else "Play Preview",
                                    tint = if (isPlaying) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    SoundManager.stopAlarm()
                    onRingtoneSelected(selectedTitle, selectedUri)
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_ringtone_button")
            ) {
                Text("Select", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    SoundManager.stopAlarm()
                    onDismiss()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}
