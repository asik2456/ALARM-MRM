package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WakeMissionType
import com.example.ui.screens.alarm_list.AlarmListScreen
import com.example.ui.screens.ringing.MathMissionScreen
import com.example.ui.screens.ringing.MorningBriefingDialog
import com.example.ui.screens.ringing.ShakeMissionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AlarmViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure flags to wake screen if alarm rings while phone is asleep/locked
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        handleAlarmIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isRinging by viewModel.isRinging.collectAsStateWithLifecycle()
            val ringingAlarm by viewModel.ringingAlarm.collectAsStateWithLifecycle()
            val showMorningBriefing by viewModel.showMorningBriefing.collectAsStateWithLifecycle()
            val currentWeather by viewModel.currentWeather.collectAsStateWithLifecycle()
            val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()

            // Request Notification Permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {}

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            MyApplicationTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (isRinging && ringingAlarm != null) {
                        // Prevent accidental back exit without completing puzzle or snoozing
                        BackHandler(enabled = true) {
                            // User must solve puzzle or tap snooze!
                        }

                        val alarm = ringingAlarm!!
                        when (alarm.missionType) {
                            WakeMissionType.MATH -> {
                                MathMissionScreen(
                                    alarm = alarm,
                                    currentTimeString = currentTime,
                                    onMissionCompleted = {
                                        viewModel.completeWakeMission()
                                    },
                                    onSnoozeClicked = {
                                        viewModel.snoozeRingingAlarm()
                                    }
                                )
                            }
                            WakeMissionType.SHAKE -> {
                                ShakeMissionScreen(
                                    alarm = alarm,
                                    currentTimeString = currentTime,
                                    onMissionCompleted = {
                                        viewModel.completeWakeMission()
                                    }
                                )
                            }
                            WakeMissionType.MEMORY -> {
                                MathMissionScreen(
                                    alarm = alarm,
                                    currentTimeString = currentTime,
                                    onMissionCompleted = {
                                        viewModel.completeWakeMission()
                                    },
                                    onSnoozeClicked = {
                                        viewModel.snoozeRingingAlarm()
                                    }
                                )
                            }
                        }
                    } else {
                        AlarmListScreen(
                            viewModel = viewModel,
                            onTestAlarm = { alarmToTest ->
                                viewModel.triggerTestAlarm(alarmToTest)
                            }
                        )
                    }

                    // Morning Briefing dialog after mission solved
                    if (showMorningBriefing) {
                        MorningBriefingDialog(
                            weather = currentWeather,
                            onDismiss = {
                                viewModel.dismissMorningBriefing()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val alarmId = intent.getLongExtra(EXTRA_RINGING_ALARM_ID, -1L)

        if (action == ACTION_ALARM_RINGING && alarmId > 0) {
            viewModel.checkAndTriggerAlarmById(alarmId)
        }
    }

    companion object {
        const val ACTION_ALARM_RINGING = "com.aistudio.alarmy.ACTION_ALARM_RINGING"
        const val EXTRA_RINGING_ALARM_ID = "EXTRA_RINGING_ALARM_ID"
    }
}
