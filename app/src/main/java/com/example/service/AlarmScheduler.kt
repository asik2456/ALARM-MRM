package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.AlarmEntity
import com.example.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun scheduleAlarm(context: Context, alarm: AlarmEntity) {
        if (!alarm.isEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTimeMs = calculateNextTriggerTime(alarm)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm ${alarm.id} for $triggerTimeMs (in ${(triggerTimeMs - System.currentTimeMillis()) / 1000}s)")
        } catch (e: SecurityException) {
            Log.e(TAG, "Exact alarm permission missing, fallback to set()", e)
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        }
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleSnooze(context: Context, alarmId: Long, snoozeMinutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTimeMs = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra("EXTRA_IS_SNOOZE", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 9999).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
        }
    }

    fun calculateNextTriggerTime(alarm: AlarmEntity): Long {
        val now = Calendar.getInstance()
        val alarmCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val days = alarm.getDaysList()
        if (days.isEmpty()) {
            // One-time alarm
            if (alarmCal.timeInMillis <= now.timeInMillis) {
                // Time has passed for today, set for tomorrow
                alarmCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return alarmCal.timeInMillis
        }

        // Repeating alarm: find the closest upcoming day
        for (i in 0..7) {
            val candidate = Calendar.getInstance().apply {
                timeInMillis = alarmCal.timeInMillis
                add(Calendar.DAY_OF_YEAR, i)
            }
            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
            if (days.contains(dayOfWeek)) {
                if (candidate.timeInMillis > now.timeInMillis) {
                    return candidate.timeInMillis
                }
            }
        }

        alarmCal.add(Calendar.DAY_OF_YEAR, 1)
        return alarmCal.timeInMillis
    }

    fun getRemainingTimeDescription(alarm: AlarmEntity): String {
        val triggerTime = calculateNextTriggerTime(alarm)
        val diffMs = triggerTime - System.currentTimeMillis()
        if (diffMs <= 0) return "Due now"

        val totalMinutes = diffMs / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "Alarm in $hours hr $minutes min"
            hours > 0 -> "Alarm in $hours hr"
            minutes > 0 -> "Alarm in $minutes min"
            else -> "Alarm in less than a minute"
        }
    }
}
