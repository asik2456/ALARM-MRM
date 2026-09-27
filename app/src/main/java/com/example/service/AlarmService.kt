package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.db.AppDatabase
import com.example.sound.SoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Acquire WakeLock so CPU doesn't sleep while alarm rings
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "WakeHero:AlarmWakeLock"
        )?.apply {
            acquire(10 * 60 * 1000L) // 10 minutes timeout safe limit
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L

        when (action) {
            ACTION_STOP_ALARM -> {
                stopForegroundAlarm()
                return START_NOT_STICKY
            }
            ACTION_START_ALARM -> {
                startForegroundWithNotification(alarmId)
                playAlarmSound(alarmId)
            }
        }

        return START_STICKY
    }

    private fun startForegroundWithNotification(alarmId: Long) {
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            this.action = MainActivity.ACTION_ALARM_RINGING
            putExtra(MainActivity.EXTRA_RINGING_ALARM_ID, alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Wake Up! Alarm Ringing")
            .setContentText("Solve the math puzzle mission to turn off your alarm!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun playAlarmSound(alarmId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val alarm = if (alarmId > 0) db.alarmDao().getAlarmById(alarmId) else null

            val ringtoneUri = alarm?.ringtoneUri ?: "preset_pulse"
            val volume = alarm?.volume ?: 0.9f
            val isGentle = alarm?.isGentleWakeUp ?: true
            val isVibe = alarm?.isVibrationEnabled ?: true

            SoundManager.startAlarm(
                context = applicationContext,
                ringtoneUri = ringtoneUri,
                targetVolume = volume,
                isGentleWakeUp = isGentle,
                enableVibration = isVibe
            )
        }
    }

    private fun stopForegroundAlarm() {
        SoundManager.stopAlarm()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Exception) {}
        stopSelf()
    }

    override fun onDestroy() {
        SoundManager.stopAlarm()
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: Exception) {}
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Loud full-screen wake up alarm ringing alerts"
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "wakehero_alarm_channel"
        const val NOTIFICATION_ID = 40401
        const val ACTION_START_ALARM = "com.aistudio.alarmy.START_ALARM"
        const val ACTION_STOP_ALARM = "com.aistudio.alarmy.STOP_ALARM"
        const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"

        fun start(context: Context, alarmId: Long) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            context.startService(intent)
        }
    }
}
