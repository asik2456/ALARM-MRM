package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.OpenMeteoService
import com.example.data.db.AppDatabase
import com.example.data.model.AlarmEntity
import com.example.data.model.DisplayWeather
import com.example.data.model.GeoLocationJson
import com.example.data.repository.AlarmRepository
import com.example.data.repository.WeatherRepository
import com.example.service.AlarmScheduler
import com.example.service.AlarmService
import com.example.sound.SoundManager
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmRepository: AlarmRepository
    private val weatherRepository: WeatherRepository

    val alarms: StateFlow<List<AlarmEntity>>

    private val _themeMode = MutableStateFlow(ThemeMode.DARK) // Default to sleek Alarmy dark mode!
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _currentWeather = MutableStateFlow<DisplayWeather?>(null)
    val currentWeather: StateFlow<DisplayWeather?> = _currentWeather.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    private val _citySearchResults = MutableStateFlow<List<GeoLocationJson>>(emptyList())
    val citySearchResults: StateFlow<List<GeoLocationJson>> = _citySearchResults.asStateFlow()

    private val _isSearchingCity = MutableStateFlow(false)
    val isSearchingCity: StateFlow<Boolean> = _isSearchingCity.asStateFlow()

    private val _ringingAlarm = MutableStateFlow<AlarmEntity?>(null)
    val ringingAlarm: StateFlow<AlarmEntity?> = _ringingAlarm.asStateFlow()

    private val _isRinging = MutableStateFlow(false)
    val isRinging: StateFlow<Boolean> = _isRinging.asStateFlow()

    private val _showMorningBriefing = MutableStateFlow(false)
    val showMorningBriefing: StateFlow<Boolean> = _showMorningBriefing.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private var clockJob: Job? = null
    private var searchJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        alarmRepository = AlarmRepository(db.alarmDao())
        val openMeteo = OpenMeteoService.create()
        weatherRepository = WeatherRepository(openMeteo)

        alarms = alarmRepository.allAlarms.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default alarms if empty
        viewModelScope.launch {
            alarmRepository.populateDefaultsIfEmpty()
        }

        // Start live digital clock tick
        startLiveClock()

        // Fetch initial weather for default city (New York)
        fetchWeather("New York", 40.7128, -74.0060)
    }

    private fun startLiveClock() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            val formatter = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            while (isActive) {
                _currentTime.value = formatter.format(Date())
                delay(1000)
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun cycleThemeMode() {
        _themeMode.value = when (_themeMode.value) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
    }

    fun saveAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            if (alarm.id == 0L) {
                val newId = alarmRepository.insertAlarm(alarm)
                val inserted = alarm.copy(id = newId)
                if (inserted.isEnabled) {
                    AlarmScheduler.scheduleAlarm(getApplication(), inserted)
                }
            } else {
                alarmRepository.updateAlarm(alarm)
                if (alarm.isEnabled) {
                    AlarmScheduler.scheduleAlarm(getApplication(), alarm)
                } else {
                    AlarmScheduler.cancelAlarm(getApplication(), alarm.id)
                }
            }
        }
    }

    fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
        viewModelScope.launch {
            alarmRepository.setAlarmEnabled(alarm.id, isEnabled)
            val updated = alarm.copy(isEnabled = isEnabled)
            if (isEnabled) {
                AlarmScheduler.scheduleAlarm(getApplication(), updated)
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), updated.id)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(getApplication(), alarm.id)
            alarmRepository.deleteAlarm(alarm)
        }
    }

    fun triggerTestAlarm(alarm: AlarmEntity) {
        _ringingAlarm.value = alarm
        _isRinging.value = true
        // Play alarm sound and vibration immediately
        SoundManager.startAlarm(
            context = getApplication(),
            ringtoneUri = alarm.ringtoneUri,
            targetVolume = alarm.volume,
            isGentleWakeUp = false, // Instant test volume
            enableVibration = alarm.isVibrationEnabled
        )
    }

    fun checkAndTriggerAlarmById(alarmId: Long) {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm != null) {
                _ringingAlarm.value = alarm
                _isRinging.value = true
            }
        }
    }

    fun completeWakeMission() {
        // Stop audio and vibration
        SoundManager.stopAlarm()
        AlarmService.stop(getApplication())
        _isRinging.value = false
        _showMorningBriefing.value = true
    }

    fun dismissMorningBriefing() {
        _showMorningBriefing.value = false
        _ringingAlarm.value = null
    }

    fun snoozeRingingAlarm() {
        val alarm = _ringingAlarm.value
        SoundManager.stopAlarm()
        AlarmService.stop(getApplication())
        _isRinging.value = false

        if (alarm != null) {
            AlarmScheduler.scheduleSnooze(getApplication(), alarm.id, alarm.snoozeMinutes)
        }
        _ringingAlarm.value = null
    }

    fun fetchWeather(cityName: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            _isWeatherLoading.value = true
            val result = weatherRepository.getWeather(cityName, lat, lon)
            result.onSuccess {
                _currentWeather.value = it
            }.onFailure {
                // If offline or network error, provide graceful fallback
                _currentWeather.value = DisplayWeather(
                    cityName = cityName,
                    temperature = 22.0,
                    condition = "Partly Cloudy",
                    weatherCode = 1
                )
            }
            _isWeatherLoading.value = false
        }
    }

    fun searchCity(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _citySearchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            _isSearchingCity.value = true
            delay(400) // Debounce
            val results = weatherRepository.searchCities(query)
            _citySearchResults.value = results
            _isSearchingCity.value = false
        }
    }

    fun getPopularCities(): List<GeoLocationJson> = weatherRepository.getPopularCities()

    override fun onCleared() {
        super.onCleared()
        clockJob?.cancel()
        searchJob?.cancel()
        SoundManager.stopAlarm()
    }
}
