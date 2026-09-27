package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MathDifficulty(val displayName: String, val description: String) {
    EASY("Easy", "Single & simple two-digit addition (e.g. 8 + 14)"),
    MEDIUM("Medium", "Three numbers & mixed operations (e.g. 23 + 47 - 16)"),
    HARD("Hard", "Multiplication & addition (e.g. 14 × 7 + 25)"),
    GENIUS("Genius", "Multi-step complex algebra (e.g. 24 × 8 - 45 / 3)")
}

enum class WakeMissionType(val displayName: String, val iconName: String) {
    MATH("Math Challenge", "calculate"),
    SHAKE("Shake Phone", "vibration"),
    MEMORY("Memory Tiles", "grid_view")
}

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int, // 0 - 23
    val minute: Int, // 0 - 59
    val label: String = "Wake Up!",
    val isEnabled: Boolean = true,
    // Days of week: 1=Sun, 2=Mon, ..., 7=Sat as comma separated string e.g. "2,3,4,5,6"
    val repeatDays: String = "", 
    val ringtoneTitle: String = "Energetic Pulse",
    val ringtoneUri: String = "preset_pulse", // preset id or content:// uri
    val volume: Float = 0.85f,
    val isGentleWakeUp: Boolean = true, // Gradual volume ramp
    val isVibrationEnabled: Boolean = true,
    val isSnoozeEnabled: Boolean = true,
    val snoozeMinutes: Int = 5,
    val missionType: WakeMissionType = WakeMissionType.MATH,
    val mathDifficulty: MathDifficulty = MathDifficulty.MEDIUM,
    val mathProblemCount: Int = 3,
    val shakeTargetCount: Int = 25,
    val memoryGridSize: Int = 4, // 4 pairs
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isRepeating(): Boolean = repeatDays.isNotBlank()

    fun getDaysList(): List<Int> {
        if (repeatDays.isBlank()) return emptyList()
        return repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    fun formattedTime(is24Hour: Boolean = false): String {
        return if (is24Hour) {
            String.format("%02d:%02d", hour, minute)
        } else {
            val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val m = String.format("%02d", minute)
            val amPm = if (hour < 12) "AM" else "PM"
            "$h:$m $amPm"
        }
    }

    fun getRepeatDaysSummary(): String {
        val days = getDaysList()
        if (days.isEmpty()) return "Once"
        if (days.size == 7) return "Every day"
        val weekdays = listOf(2, 3, 4, 5, 6)
        val weekends = listOf(1, 7)
        if (days.containsAll(weekdays) && days.size == 5) return "Weekdays"
        if (days.containsAll(weekends) && days.size == 2) return "Weekends"

        val dayNames = mapOf(
            1 to "Sun", 2 to "Mon", 3 to "Tue", 4 to "Wed",
            5 to "Thu", 6 to "Fri", 7 to "Sat"
        )
        return days.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }
}
