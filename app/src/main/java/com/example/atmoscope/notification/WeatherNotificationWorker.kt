package com.example.atmoscope.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.atmoscope.MainActivity
import com.example.atmoscope.R
import com.example.atmoscope.data.RetrofitInstance
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "atmoscope_weather_alerts"
        const val CHANNEL_NAME = "Peringatan Cuaca"
        const val UV_EXTREME = 999
    }

    override suspend fun doWork(): Result {
        return try {
            val prefs = context.getSharedPreferences("atmoscope_prefs", Context.MODE_PRIVATE)

            val notifEnabled = prefs.getBoolean("notif_enabled", true)
            if (!notifEnabled) return Result.success()

            val lat = prefs.getFloat("last_lat", 0f).toDouble()
            val lon = prefs.getFloat("last_lon", 0f).toDouble()
            val timezone = prefs.getString("last_timezone", "Asia/Jakarta") ?: "Asia/Jakarta"
            val cityName = prefs.getString("last_gps_city", "")
                ?: prefs.getString("last_manual_city", "Kota Kamu")
                ?: "Kota Kamu"

            if (lat == 0.0 && lon == 0.0) return Result.success()

            // Fetch hourly forecast
            val forecast = RetrofitInstance.weatherApi.getForecast(
                latitude = lat,
                longitude = lon,
                timezone = timezone,
                daily = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_sum"
            )

            val todayUv = forecast.daily.uvIndex.firstOrNull() ?: 0.0
            if (todayUv > 8) {
                sendNotification(cityName, UV_EXTREME, "--")
            }

            // Cari weather code ekstrem hari ini
            val today = LocalDate.now().toString()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

            // Filter jam-jam hari ini saja
            val todayIndices = forecast.hourly.time.indices.filter { i ->
                forecast.hourly.time[i].startsWith(today)
            }

            // Cari jam pertama cuaca ekstrem hari ini
            var firstExtremeCode: Int? = null
            var firstExtremeTime: String? = null

            for (i in todayIndices) {
                val code = forecast.hourly.weatherCode[i]
                if (isExtremeWeather(code)) {
                    firstExtremeCode = code
                    firstExtremeTime = forecast.hourly.time[i].substring(11, 16) // HH:mm
                    break
                }
            }

            if (firstExtremeCode != null && firstExtremeTime != null) {
                sendNotification(cityName, firstExtremeCode, firstExtremeTime)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun isExtremeWeather(code: Int): Boolean = code in listOf(
        61, 63, 65,      // Hujan
        80, 81, 82,      // Hujan lebat
        95, 96, 99       // Badai petir
    )

    private fun sendNotification(cityName: String, weatherCode: Int, time: String) {
        val (title, message) = getNotificationContent(cityName, weatherCode, time)
        if (title.isEmpty()) return
        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(
                if (weatherCode in listOf(95, 96, 99, 80, 81, 82))
                    NotificationCompat.PRIORITY_HIGH
                else
                    NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notifManager.notify(weatherCode, notification)
    }

    private fun getNotificationContent(
        city: String,
        code: Int,
        time: String
    ): Pair<String, String> = when (code) {
        95, 96, 99 -> Pair(
            "⛈️ Peringatan Cuaca Ekstrem — $city",
            "Badai petir diprediksi sekitar pukul $time hari ini. Hindari aktivitas luar ruangan."
        )
        80, 81, 82 -> Pair(
            "🌧️ Hujan Lebat — $city",
            "Hujan lebat diprediksi sekitar pukul $time hari ini. Jangan lupa bawa payung!"
        )
        61, 63, 65 -> Pair(
            "☂️ Siapkan Payung — $city",
            "Hujan diprediksi sekitar pukul $time di $city. Jangan lupa bawa payung!"
        )
        UV_EXTREME -> Pair(
            "☀️ Cuaca Terik Hari Ini — $city",
            "Indeks UV sangat tinggi hari ini. Pakai sunscreen dan hindari matahari siang."
        )
        else -> Pair("", "")
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Peringatan cuaca ekstrem dari Atmoscope"
        }
        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notifManager.createNotificationChannel(channel)
    }
}