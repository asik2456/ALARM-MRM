package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val current: CurrentWeatherJson? = null,
    val daily: DailyWeatherJson? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherJson(
    val time: String? = null,
    @Json(name = "temperature_2m") val temperature: Double = 0.0,
    @Json(name = "relative_humidity_2m") val humidity: Int = 0,
    @Json(name = "apparent_temperature") val apparentTemperature: Double = 0.0,
    @Json(name = "weather_code") val weatherCode: Int = 0,
    @Json(name = "wind_speed_10m") val windSpeed: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class DailyWeatherJson(
    val time: List<String>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "temperature_2m_max") val tempMax: List<Double>? = null,
    @Json(name = "temperature_2m_min") val tempMin: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeoLocationJson>? = null
)

@JsonClass(generateAdapter = true)
data class GeoLocationJson(
    val id: Long? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
)

data class DisplayWeather(
    val cityName: String = "Local Weather",
    val temperature: Double = 21.0,
    val feelsLike: Double = 22.0,
    val minTemp: Double = 16.0,
    val maxTemp: Double = 26.0,
    val humidity: Int = 55,
    val windSpeed: Double = 12.0,
    val weatherCode: Int = 0,
    val condition: String = "Clear Sky",
    val isDay: Boolean = true,
    val quote: String = "Wake up with determination, go to bed with satisfaction!"
) {
    fun formattedTemp(): String = "${temperature.toInt()}°C"
    fun formattedRange(): String = "H: ${maxTemp.toInt()}°  L: ${minTemp.toInt()}°"
}

fun getWeatherConditionFromCode(code: Int): Pair<String, String> {
    return when (code) {
        0 -> "Clear Sky" to "sunny"
        1, 2 -> "Partly Cloudy" to "cloud_sun"
        3 -> "Overcast" to "cloudy"
        45, 48 -> "Foggy" to "foggy"
        51, 53, 55 -> "Light Drizzle" to "rainy"
        61, 63, 65 -> "Rain" to "rainy"
        71, 73, 75, 77 -> "Snow" to "snow"
        80, 81, 82 -> "Rain Showers" to "rainy"
        85, 86 -> "Snow Showers" to "snow"
        95, 96, 99 -> "Thunderstorm" to "thunderstorm"
        else -> "Clear" to "sunny"
    }
}
