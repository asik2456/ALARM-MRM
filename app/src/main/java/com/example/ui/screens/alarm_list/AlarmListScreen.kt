package com.example.ui.screens.alarm_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AlarmEntity
import com.example.service.AlarmScheduler
import com.example.ui.screens.alarm_edit.AlarmEditSheet
import com.example.ui.screens.weather.WeatherCityDialog
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    viewModel: AlarmViewModel,
    onTestAlarm: (AlarmEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val weather by viewModel.currentWeather.collectAsStateWithLifecycle()
    val isWeatherLoading by viewModel.isWeatherLoading.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()

    val citySearchResults by viewModel.citySearchResults.collectAsStateWithLifecycle()
    val isSearchingCity by viewModel.isSearchingCity.collectAsStateWithLifecycle()

    var showEditSheet by remember { mutableStateOf(false) }
    var selectedAlarmForEdit by remember { mutableStateOf<AlarmEntity?>(null) }
    var showCityDialog by remember { mutableStateOf(false) }

    // Find next upcoming alarm for banner
    val nextAlarm = alarms.filter { it.isEnabled }
        .minByOrNull { AlarmScheduler.calculateNextTriggerTime(it) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("alarm_list_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "WakeHero",
                                tint = NeonOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "WakeHero",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (currentTime.isNotBlank()) {
                                Text(
                                    text = currentTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Dark Mode / Theme Toggle Button
                    IconButton(
                        onClick = { viewModel.cycleThemeMode() },
                        modifier = Modifier.testTag("theme_mode_toggle_button")
                    ) {
                        val icon = when (themeMode) {
                            ThemeMode.DARK -> Icons.Default.DarkMode
                            ThemeMode.LIGHT -> Icons.Default.LightMode
                            ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Switch Dark/Light Theme: $themeMode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedAlarmForEdit = null
                    showEditSheet = true
                },
                modifier = Modifier.testTag("add_alarm_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Alarm")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Add Alarm", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Weather Widget Card
            item {
                WeatherWidget(
                    weather = weather,
                    isLoading = isWeatherLoading,
                    onLocationClick = { showCityDialog = true },
                    onRefreshClick = {
                        val w = weather
                        if (w != null) {
                            viewModel.fetchWeather(w.cityName, 40.7128, -74.0060)
                        }
                    }
                )
            }

            // Next Alarm Banner
            if (nextAlarm != null) {
                item {
                    val desc = AlarmScheduler.getRemainingTimeDescription(nextAlarm)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("next_alarm_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Next Alarm: ${nextAlarm.formattedTime()} ($desc)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Alarms",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${alarms.size} total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Alarms List or Empty State
            if (alarms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Alarms Set",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Add Alarm' to configure your wake-up time and math challenge mission.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { isEnabled ->
                            viewModel.toggleAlarm(alarm, isEnabled)
                        },
                        onClick = {
                            selectedAlarmForEdit = alarm
                            showEditSheet = true
                        },
                        onDelete = {
                            viewModel.deleteAlarm(alarm)
                        },
                        onTestNow = {
                            onTestAlarm(alarm)
                        }
                    )
                }
            }
        }
    }

    // Alarm Edit Sheet Modal
    if (showEditSheet) {
        AlarmEditSheet(
            alarm = selectedAlarmForEdit,
            onSave = { updated ->
                viewModel.saveAlarm(updated)
            },
            onTestAlarm = { alarmToTest ->
                showEditSheet = false
                onTestAlarm(alarmToTest)
            },
            onDismiss = { showEditSheet = false }
        )
    }

    // Weather City Selector Dialog
    if (showCityDialog) {
        WeatherCityDialog(
            popularCities = viewModel.getPopularCities(),
            searchResults = citySearchResults,
            isSearching = isSearchingCity,
            onSearchQueryChange = { query ->
                viewModel.searchCity(query)
            },
            onCitySelected = { city ->
                viewModel.fetchWeather(city.name, city.latitude, city.longitude)
            },
            onDismiss = { showCityDialog = false }
        )
    }
}
