package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.service.AlarmScheduler
import com.example.service.AlarmService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_FIRE_ALARM = "com.aistudio.alarmy.ACTION_FIRE_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.aistudio.alarmy.ACTION_SNOOZE_ALARM"
        const val ACTION_DISMISS_ALARM = "com.aistudio.alarmy.ACTION_DISMISS_ALARM"
        const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "onReceive intent action: ${intent?.action}")
        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L

        when (intent?.action) {
            ACTION_FIRE_ALARM -> {
                // Start Foreground Service to sound alarm
                AlarmService.start(context, alarmId)

                // Check if repeating, otherwise turn off or reschedule next cycle
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(context)
                    val alarm = db.alarmDao().getAlarmById(alarmId)
                    if (alarm != null) {
                        if (alarm.isRepeating()) {
                            // Reschedule for next week occurrence
                            AlarmScheduler.scheduleAlarm(context, alarm)
                        } else {
                            // Single time alarm: disable after firing
                            db.alarmDao().updateAlarmEnabled(alarm.id, false)
                        }
                    }
                }
            }
            ACTION_SNOOZE_ALARM -> {
                val snoozeMinutes = intent.getIntExtra("EXTRA_SNOOZE_MINUTES", 5)
                AlarmService.stop(context)
                AlarmScheduler.scheduleSnooze(context, alarmId, snoozeMinutes)
            }
            ACTION_DISMISS_ALARM -> {
                AlarmService.stop(context)
            }
        }
    }
}
