package com.lnxteam.atmoscope.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.withTimeoutOrNull

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val districtName: String,
    val timezone: String
)

class AppLocationManager(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun isLocationEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult? =
        withTimeoutOrNull(15000L) {
            suspendCancellableCoroutine { cont ->
                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 5000L
                ).setMaxUpdates(1).build()

                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                        fusedClient.removeLocationUpdates(this)
                        val loc = result.lastLocation
                        if (loc != null) {
                            cont.resume(reverseGeocode(loc))
                        } else {
                            cont.resume(null)
                        }
                    }
                }

                fusedClient.requestLocationUpdates(
                    request, callback, Looper.getMainLooper()
                )

                cont.invokeOnCancellation {
                    fusedClient.removeLocationUpdates(callback)
                }
            }
        }

    private fun reverseGeocode(location: Location): LocationResult? {
        return try {
            val geocoder = Geocoder(context, Locale("id", "ID"))
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses.isNullOrEmpty()) {
                // Geocoder gagal, tetap return dengan nama fallback
                LocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = "Lokasi Saya",
                    districtName = "",
                    timezone = inferTimezone(location.longitude)
                )
            } else {
                val addr = addresses[0]
                // Android Geocoder field mapping:
                // subLocality / locality = kecamatan/kelurahan
                // subAdminArea = kota/kabupaten
                // adminArea = provinsi
                val district = addr.subLocality
                    ?: addr.locality
                    ?: addr.subAdminArea
                    ?: ""
                val city = addr.subAdminArea
                    ?: addr.adminArea
                    ?: "Lokasi Saya"

                LocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = city,
                    districtName = district,
                    timezone = inferTimezone(location.longitude)
                )
            }
        } catch (e: Exception) {
            LocationResult(
                latitude = location.latitude,
                longitude = location.longitude,
                cityName = "Lokasi Saya",
                districtName = "",
                timezone = inferTimezone(location.longitude)
            )
        }
    }

    // Inferensi timezone berdasarkan longitude Indonesia
    private fun inferTimezone(longitude: Double): String = when {
        longitude < 115.0 -> "Asia/Jakarta"
        longitude < 135.0 -> "Asia/Makassar"
        else -> "Asia/Jayapura"
    }
}