package com.example.ui.screens.ringing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmEntity
import com.example.data.model.MathDifficulty
import com.example.ui.components.NumberKeypad
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.NeonOrange
import com.example.util.MathProblem
import com.example.util.MathPuzzleGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MathMissionScreen(
    alarm: AlarmEntity,
    currentTimeString: String,
    onMissionCompleted: () -> Unit,
    onSnoozeClicked: () -> Unit
) {
    val totalProblems = alarm.mathProblemCount.coerceAtLeast(1)
    var currentProblemIndex by remember { mutableIntStateOf(0) }
    var currentProblem by remember {
        mutableStateOf(MathPuzzleGenerator.generateProblem(alarm.mathDifficulty))
    }
    var userInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessFlash by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Pulse animation for the ringing alarm bell
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        pulseScale.animateTo(
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("math_mission_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Ringing Status & Time
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(NeonOrange.copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alarm Active",
                        tint = NeonOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ALARM RINGING",
                        color = NeonOrange,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentTimeString,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            // Challenge Box: Problem progress, expression, input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 2.dp,
                        color = when {
                            isSuccessFlash -> MintSuccess
                            errorMessage != null -> CrimsonAlert
                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress counter & bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Problem ${currentProblemIndex + 1} of $totalProblems",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${alarm.mathDifficulty.displayName} Math",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (currentProblemIndex) / totalProblems.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Math Expression
                Text(
                    text = "${currentProblem.expression} = ?",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("math_expression_text")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // User Input Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 2.dp,
                            color = if (errorMessage != null) CrimsonAlert else MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (userInput.isEmpty()) "Tap answer" else userInput,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (userInput.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("math_user_input_text")
                    )
                }

                // Error or Success Banner
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .padding(top = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = CrimsonAlert,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    } else if (isSuccessFlash) {
                        Text(
                            text = "Correct! Good job! 🎉",
                            color = MintSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Keypad & Snooze control
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumberKeypad(
                    onDigitClick = { digit ->
                        if (userInput.length < 6) {
                            userInput += digit
                            errorMessage = null
                        }
                    },
                    onBackspaceClick = {
                        if (userInput.isNotEmpty()) {
                            userInput = userInput.dropLast(1)
                            errorMessage = null
                        }
                    },
                    onSubmitClick = {
                        val enteredVal = userInput.toIntOrNull()
                        if (enteredVal == null) {
                            errorMessage = "Please enter your answer"
                            return@NumberKeypad
                        }

                        if (enteredVal == currentProblem.solution) {
                            // Correct!
                            errorMessage = null
                            isSuccessFlash = true
                            coroutineScope.launch {
                                delay(600)
                                isSuccessFlash = false
                                userInput = ""
                                if (currentProblemIndex + 1 >= totalProblems) {
                                    // Finished all problems! Stop alarm & complete mission!
                                    onMissionCompleted()
                                } else {
                                    currentProblemIndex++
                                    currentProblem = MathPuzzleGenerator.generateProblem(alarm.mathDifficulty)
                                }
                            }
                        } else {
                            // Incorrect answer
                            errorMessage = "Incorrect ($enteredVal). Try again!"
                            userInput = ""
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Snooze Button
                if (alarm.isSnoozeEnabled) {
                    OutlinedButton(
                        onClick = onSnoozeClicked,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(46.dp)
                            .testTag("snooze_alarm_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = "Snooze Alarm",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Snooze for ${alarm.snoozeMinutes} mins",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
