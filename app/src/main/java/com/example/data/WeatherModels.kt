package com.example.data

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather?
)

data class CurrentWeather(
    val time: String? = null,
    val interval: Int? = null,
    val temperature_2m: Double = 20.0,
    val relative_humidity_2m: Int = 50,
    val weather_code: Int = 0,
    val uv_index: Double? = 0.0,
    val surface_solar_radiation: Double? = 0.0,
    val precipitation: Double? = 0.0,
    val rain: Double? = 0.0
)

data class CityLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

object DefaultCities {
    val cities = listOf(
        CityLocation("New York", "USA", 40.7128, -74.0060),
        CityLocation("London", "UK", 51.5074, -0.1278),
        CityLocation("Tokyo", "Japan", 35.6762, 139.6503),
        CityLocation("Paris", "France", 48.8566, 2.3522),
        CityLocation("Sydney", "Australia", -33.8688, 151.2093),
        CityLocation("San Francisco", "USA", 37.7749, -122.4194),
        CityLocation("Singapore", "Singapore", 1.3521, 103.8198),
        CityLocation("Toronto", "Canada", 43.6532, -79.3832),
        CityLocation("Berlin", "Germany", 52.5200, 13.4050)
    )
}
