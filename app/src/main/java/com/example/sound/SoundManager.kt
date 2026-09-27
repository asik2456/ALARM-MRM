package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

data class SoundPreset(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String
)

object SoundManager {
    private const val TAG = "SoundManager"

    val PRESETS = listOf(
        SoundPreset("preset_pulse", "Energetic Pulse", "Loud, high-tempo alarm bursts", "bolt"),
        SoundPreset("preset_siren", "Siren Alert", "Emergency siren wail to break deep sleep", "warning"),
        SoundPreset("preset_rooster", "Rooster Wakeup", "Multi-frequency loud crowing sound", "wb_sunny"),
        SoundPreset("preset_beep", "Classic Digital", "High-frequency rapid beep sequence", "alarm"),
        SoundPreset("preset_chime", "Sunrise Melodic", "Gentle ascending harmonic chimes", "music_note"),
        SoundPreset("preset_klaxon", "Extreme Klaxon", "Deep aggressive dual-frequency klaxon", "volume_up"),
        SoundPreset("system_alarm", "System Default Alarm", "Your phone's default alarm tone", "phone_android"),
        SoundPreset("system_ringtone", "System Ringtone", "Your phone's default phone ringtone", "ring_volume")
    )

    private var mediaPlayer: MediaPlayer? = null
    private var synthesizedTrack: AudioTrack? = null
    private var vibrator: Vibrator? = null
    private var soundJob: Job? = null
    private var rampJob: Job? = null
    private var isPlaying = false

    fun startAlarm(
        context: Context,
        ringtoneUri: String,
        targetVolume: Float,
        isGentleWakeUp: Boolean,
        enableVibration: Boolean
    ) {
        stopAlarm()
        isPlaying = true

        val initialVolume = if (isGentleWakeUp) (targetVolume * 0.2f).coerceAtLeast(0.1f) else targetVolume
        var currentVolume = initialVolume

        // Start vibration
        if (enableVibration) {
            startVibration(context)
        }

        // Start Sound
        when {
            ringtoneUri.startsWith("preset_") -> {
                startSynthesizedPreset(ringtoneUri, initialVolume)
            }
            ringtoneUri == "system_alarm" -> {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                startMediaPlayer(context, uri, initialVolume)
            }
            ringtoneUri == "system_ringtone" -> {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                startMediaPlayer(context, uri, initialVolume)
            }
            else -> {
                try {
                    val parsed = Uri.parse(ringtoneUri)
                    startMediaPlayer(context, parsed, initialVolume)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse uri $ringtoneUri, fallback to pulse", e)
                    startSynthesizedPreset("preset_pulse", initialVolume)
                }
            }
        }

        // Volume ramp-up for Gentle Wake-up
        if (isGentleWakeUp && targetVolume > initialVolume) {
            rampJob = CoroutineScope(Dispatchers.Main).launch {
                val steps = 20
                val durationMs = 25000L // 25 seconds ramp
                val stepInterval = durationMs / steps
                val volumeDelta = (targetVolume - initialVolume) / steps

                for (i in 1..steps) {
                    delay(stepInterval)
                    if (!isPlaying) break
                    currentVolume += volumeDelta
                    setVolume(currentVolume.coerceAtMost(targetVolume))
                }
            }
        }
    }

    private fun setVolume(volume: Float) {
        mediaPlayer?.let {
            try {
                it.setVolume(volume, volume)
            } catch (_: Exception) {}
        }
        synthesizedTrack?.let {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    it.setVolume(volume)
                }
            } catch (_: Exception) {}
        }
    }

    private fun startMediaPlayer(context: Context, uri: Uri, volume: Float) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                setVolume(volume, volume)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer failed with URI $uri, fallback to synthesized tone", e)
            startSynthesizedPreset("preset_pulse", volume)
        }
    }

    private fun startSynthesizedPreset(presetId: String, volume: Float) {
        val sampleRate = 44100
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = minBufferSize.coerceAtLeast(sampleRate / 2)

        try {
            synthesizedTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            synthesizedTrack?.setVolume(volume)
            synthesizedTrack?.play()

            soundJob = CoroutineScope(Dispatchers.Default).launch {
                val samples = ShortArray(bufferSize)
                var phase = 0.0

                while (isActive && isPlaying) {
                    val timeMs = System.currentTimeMillis()
                    // Tone frequency generation based on preset
                    val freq = when (presetId) {
                        "preset_pulse" -> {
                            val cycle = (timeMs % 800)
                            if (cycle < 400) 880.0 else 0.0 // 880Hz beep
                        }
                        "preset_siren" -> {
                            // Siren sweeps between 600Hz and 1300Hz every 1.2s
                            val sweep = ((timeMs % 1200) / 1200.0)
                            600.0 + 700.0 * sin(sweep * PI)
                        }
                        "preset_rooster" -> {
                            val cycle = (timeMs % 2000)
                            when {
                                cycle < 300 -> 523.25 // C5
                                cycle < 600 -> 659.25 // E5
                                cycle < 1100 -> 783.99 // G5
                                cycle < 1400 -> 1046.50 // C6
                                else -> 0.0
                            }
                        }
                        "preset_beep" -> {
                            val cycle = (timeMs % 500)
                            if (cycle < 200) 1200.0 else 0.0
                        }
                        "preset_chime" -> {
                            val cycle = (timeMs % 2500)
                            when {
                                cycle < 400 -> 440.0 // A4
                                cycle < 800 -> 554.37 // C#5
                                cycle < 1200 -> 659.25 // E5
                                cycle < 1800 -> 880.0 // A5
                                else -> 0.0
                            }
                        }
                        "preset_klaxon" -> {
                            val cycle = (timeMs % 1000)
                            if (cycle < 500) 440.0 else 330.0
                        }
                        else -> 800.0
                    }

                    for (i in samples.indices) {
                        if (freq <= 0.0) {
                            samples[i] = 0
                        } else {
                            val angularFreq = 2.0 * PI * freq / sampleRate
                            phase += angularFreq
                            if (phase > 2 * PI) phase -= 2 * PI
                            samples[i] = (sin(phase) * Short.MAX_VALUE * 0.9).toInt().toShort()
                        }
                    }

                    synthesizedTrack?.write(samples, 0, samples.size)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "AudioTrack init failed", e)
        }
    }

    private fun startVibration(context: Context) {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            val pattern = longArrayOf(0, 600, 300, 600, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to vibrate", e)
        }
    }

    fun stopAlarm() {
        isPlaying = false
        rampJob?.cancel()
        rampJob = null
        soundJob?.cancel()
        soundJob = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            synthesizedTrack?.stop()
            synthesizedTrack?.release()
        } catch (_: Exception) {}
        synthesizedTrack = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
        vibrator = null
    }

    fun previewSound(context: Context, ringtoneUri: String, volume: Float) {
        startAlarm(context, ringtoneUri, volume, isGentleWakeUp = false, enableVibration = false)
        CoroutineScope(Dispatchers.Main).launch {
            delay(4000)
            if (isPlaying) {
                stopAlarm()
            }
        }
    }
}
