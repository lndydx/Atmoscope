package com.lnxteam.atmoscope.data

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather,
    val hourly: HourlyWeather,
    val daily: DailyWeather
)

data class CurrentWeather(
    val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("apparent_temperature") val feelsLike: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    val precipitation: Double,
    val cloudcover: Int
)

data class HourlyWeather(
    val time: List<String>,
    @SerializedName("temperature_2m") val temperature: List<Double>,
    @SerializedName("weather_code") val weatherCode: List<Int>,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int>,
    val cloudcover: List<Int>
)

data class DailyWeather(
    val time: List<String>,
    @SerializedName("weather_code") val weatherCode: List<Int>,
    @SerializedName("temperature_2m_max") val tempMax: List<Double>,
    @SerializedName("temperature_2m_min") val tempMin: List<Double>,
    val sunrise: List<String>,
    val sunset: List<String>,
    @SerializedName("uv_index_max") val uvIndex: List<Double>,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double>
)

data class AirQualityResponse(
    val hourly: AirQualityHourly
)

data class AirQualityHourly(
    val time: List<String>,
    @SerializedName("european_aqi") val aqi: List<Int?>
)

fun Int.toWeatherLabel(): String = when (this) {
    0 -> "Cerah"
    1, 2 -> "Cerah Berawan"
    3 -> "Berawan"
    45, 48 -> "Berkabut"
    51, 53, 55 -> "Gerimis"
    61, 63, 65 -> "Hujan"
    71, 73, 75 -> "Salju"
    80, 81, 82 -> "Hujan Lebat"
    95 -> "Badai"
    96, 99 -> "Badai Petir"
    else -> "Tidak Diketahui"
}

fun Int.toLottieAsset(): String = when (this) {
    0 -> "lottie_sunny.json"
    1, 2 -> "lottie_partly_cloudy.json"
    3 -> "lottie_cloudy.json"
    45, 48 -> "lottie_fog.json"
    51, 53, 55, 61, 63, 65, 80, 81, 82 -> "lottie_rain.json"
    95, 96, 99 -> "lottie_thunder.json"
    else -> "lottie_cloudy.json"
}

fun calculateSkyScore(cloudcover: Int, aqi: Int?): Int {
    val cloudScore = (100 - cloudcover)
    val aqiPenalty = when {
        aqi == null -> 0
        aqi < 20 -> 0
        aqi < 40 -> 10
        aqi < 60 -> 20
        else -> 30
    }
    return (cloudScore - aqiPenalty).coerceIn(0, 100)
}