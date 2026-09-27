package com.example.data.repository

import com.example.data.api.OpenMeteoService
import com.example.data.model.DisplayWeather
import com.example.data.model.GeoLocationJson
import com.example.data.model.getWeatherConditionFromCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val apiService: OpenMeteoService) {

    private val morningQuotes = listOf(
        "Wake up with determination, go to bed with satisfaction!",
        "Every morning is a new canvas to paint your story.",
        "Your future is created by what you do today, not tomorrow.",
        "Conquer the morning, conquer the day!",
        "Rise up, start fresh, see the bright opportunity in each day.",
        "Today is full of possibilities. Make it count!"
    )

    suspend fun getWeather(cityName: String, lat: Double, lon: Double): Result<DisplayWeather> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getForecast(latitude = lat, longitude = lon)
                val current = response.current
                val daily = response.daily

                val temp = current?.temperature ?: 21.0
                val feelsLike = current?.apparentTemperature ?: temp
                val humidity = current?.humidity ?: 50
                val wind = current?.windSpeed ?: 10.0
                val code = current?.weatherCode ?: 0

                val maxTemp = daily?.tempMax?.firstOrNull() ?: (temp + 4.0)
                val minTemp = daily?.tempMin?.firstOrNull() ?: (temp - 4.0)

                val (conditionName, _) = getWeatherConditionFromCode(code)
                val quote = morningQuotes.random()

                Result.success(
                    DisplayWeather(
                        cityName = cityName,
                        temperature = temp,
                        feelsLike = feelsLike,
                        minTemp = minTemp,
                        maxTemp = maxTemp,
                        humidity = humidity,
                        windSpeed = wind,
                        weatherCode = code,
                        condition = conditionName,
                        isDay = true,
                        quote = quote
                    )
                )
            } catch (e: Exception) {
                // Return gracefully with default fallback or error
                Result.failure(e)
            }
        }
    }

    suspend fun searchCities(query: String): List<GeoLocationJson> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchLocations(query)
                response.results ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun getPopularCities(): List<GeoLocationJson> {
        return listOf(
            GeoLocationJson(id = 1, name = "New York", latitude = 40.7128, longitude = -74.0060, country = "United States", admin1 = "New York"),
            GeoLocationJson(id = 2, name = "London", latitude = 51.5074, longitude = -0.1278, country = "United Kingdom", admin1 = "England"),
            GeoLocationJson(id = 3, name = "Tokyo", latitude = 35.6762, longitude = 139.6503, country = "Japan", admin1 = "Tokyo"),
            GeoLocationJson(id = 4, name = "Paris", latitude = 48.8566, longitude = 2.3522, country = "France", admin1 = "Île-de-France"),
            GeoLocationJson(id = 5, name = "San Francisco", latitude = 37.7749, longitude = -122.4194, country = "United States", admin1 = "California"),
            GeoLocationJson(id = 6, name = "Sydney", latitude = -33.8688, longitude = 151.2093, country = "Australia", admin1 = "New South Wales"),
            GeoLocationJson(id = 7, name = "Berlin", latitude = 52.5200, longitude = 13.4050, country = "Germany", admin1 = "Berlin"),
            GeoLocationJson(id = 8, name = "Mumbai", latitude = 19.0760, longitude = 72.8777, country = "India", admin1 = "Maharashtra")
        )
    }
}
