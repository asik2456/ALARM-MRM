package com.example.data.repository

import com.example.data.db.AlarmDao
import com.example.data.model.AlarmEntity
import com.example.data.model.MathDifficulty
import com.example.data.model.WakeMissionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AlarmRepository(private val alarmDao: AlarmDao) {
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

    suspend fun getAlarmById(id: Long): AlarmEntity? = alarmDao.getAlarmById(id)

    suspend fun getEnabledAlarms(): List<AlarmEntity> = alarmDao.getEnabledAlarms()

    suspend fun insertAlarm(alarm: AlarmEntity): Long = alarmDao.insertAlarm(alarm)

    suspend fun updateAlarm(alarm: AlarmEntity) = alarmDao.updateAlarm(alarm)

    suspend fun deleteAlarm(alarm: AlarmEntity) = alarmDao.deleteAlarm(alarm)

    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)

    suspend fun setAlarmEnabled(id: Long, isEnabled: Boolean) =
        alarmDao.updateAlarmEnabled(id, isEnabled)

    suspend fun populateDefaultsIfEmpty() {
        val current = allAlarms.firstOrNull()
        if (current.isNullOrEmpty()) {
            val defaultAlarm1 = AlarmEntity(
                hour = 7,
                minute = 0,
                label = "Energize & Conquer",
                isEnabled = true,
                repeatDays = "2,3,4,5,6", // Mon-Fri
                ringtoneTitle = "Energetic Pulse",
                ringtoneUri = "preset_pulse",
                volume = 0.9f,
                isGentleWakeUp = true,
                isVibrationEnabled = true,
                missionType = WakeMissionType.MATH,
                mathDifficulty = MathDifficulty.MEDIUM,
                mathProblemCount = 3
            )
            val defaultAlarm2 = AlarmEntity(
                hour = 8,
                minute = 30,
                label = "Weekend Recharge",
                isEnabled = false,
                repeatDays = "1,7", // Sat-Sun
                ringtoneTitle = "Sunrise Melodic",
                ringtoneUri = "preset_chime",
                volume = 0.8f,
                isGentleWakeUp = true,
                isVibrationEnabled = true,
                missionType = WakeMissionType.MATH,
                mathDifficulty = MathDifficulty.EASY,
                mathProblemCount = 2
            )
            insertAlarm(defaultAlarm1)
            insertAlarm(defaultAlarm2)
        }
    }
}
