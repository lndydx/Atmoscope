package com.lnxteam.atmoscope.data

import android.content.SharedPreferences
import com.google.gson.Gson
import com.lnxteam.atmoscope.viewmodel.WeatherBundle

data class CachedWeather(
    val bundle: WeatherBundle,
    val cityName: String,
    val districtName: String,
    val timestamp: Long,
    val isGpsLocation: Boolean
)

class WeatherCache(private val prefs: SharedPreferences) {

    private val gson = Gson()

    fun save(cached: CachedWeather) {
        prefs.edit()
            .putString("cached_weather", gson.toJson(cached))
            .apply()
    }

    fun load(): CachedWeather? {
        val json = prefs.getString("cached_weather", null) ?: return null
        return try {
            gson.fromJson(json, CachedWeather::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getAgeString(timestamp: Long): String {
        val diffMs = System.currentTimeMillis() - timestamp
        val diffMin = diffMs / 60_000
        return when {
            diffMin < 1 -> "baru saja"
            diffMin < 60 -> "$diffMin menit lalu"
            diffMin < 1440 -> "${diffMin / 60} jam lalu"
            else -> "${diffMin / 1440} hari lalu"
        }
    }
}