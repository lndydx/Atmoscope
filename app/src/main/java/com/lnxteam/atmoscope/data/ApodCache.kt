package com.lnxteam.atmoscope.data

import android.content.SharedPreferences
import com.google.gson.Gson
import java.time.LocalDate

class ApodCache(private val prefs: SharedPreferences) {

    private val gson = Gson()

    fun save(response: ApodResponse) {
        prefs.edit()
            .putString("apod_${response.date}", gson.toJson(response))
            .apply()
    }

    fun loadToday(): ApodResponse? {
        val today = LocalDate.now().toString()
        val json = prefs.getString("apod_$today", null) ?: return null
        return try {
            gson.fromJson(json, ApodResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }
}