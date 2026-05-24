package com.example.atmoscope.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.atmoscope.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

data class CityInfo(val lat: Double, val lon: Double, val timezone: String)

data class WeatherBundle(
    val forecast: ForecastResponse,
    val airQuality: AirQualityResponse
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("atmoscope_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val weatherCache = WeatherCache(prefs)
    val locationManager = AppLocationManager(application)

    private val _lastGpsLocationName = MutableStateFlow(
        prefs.getString("last_gps_city", "") ?: ""
    )
    val lastGpsLocationName: StateFlow<String> = _lastGpsLocationName

    private val _lastGpsDistrictName = MutableStateFlow(
        prefs.getString("last_gps_district", "") ?: ""
    )
    val lastGpsDistrictName: StateFlow<String> = _lastGpsDistrictName

    private val _isDetectingLocation = MutableStateFlow(false)
    val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation

    // ── State ──────────────────────────────────────────────
    private val _weatherState = MutableStateFlow<UiState<WeatherBundle>>(UiState.Idle)
    val weatherState: StateFlow<UiState<WeatherBundle>> = _weatherState

    private val _selectedCity = MutableStateFlow("")
    val selectedCity: StateFlow<String> = _selectedCity

    private val _selectedDistrict = MutableStateFlow("")
    val selectedDistrict: StateFlow<String> = _selectedDistrict

    private val _savedCities = MutableStateFlow<List<String>>(loadSavedCities())
    val savedCities: StateFlow<List<String>> = _savedCities

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _tempUnit = MutableStateFlow("C")
    val tempUnit: StateFlow<String> = _tempUnit

    private val _isAstroMode = MutableStateFlow(false)
    val isAstroMode: StateFlow<Boolean> = _isAstroMode

    // GPS state
    private val _isUsingGps = MutableStateFlow(prefs.getBoolean("is_using_gps", true))
    val isUsingGps: StateFlow<Boolean> = _isUsingGps

    private val _isGpsLocation = MutableStateFlow(false)
    val isGpsLocation: StateFlow<Boolean> = _isGpsLocation

    // Cache info
    private val _cacheTimestamp = MutableStateFlow<Long?>(null)
    val cacheTimestamp: StateFlow<Long?> = _cacheTimestamp

    private val _isFromCache = MutableStateFlow(false)
    val isFromCache: StateFlow<Boolean> = _isFromCache

    // ── Kota Indonesia ─────────────────────────────────────
    val cities = mapOf(
        "Banda Aceh"       to CityInfo(5.55,   95.32,  "Asia/Jakarta"),
        "Medan"            to CityInfo(3.59,   98.67,  "Asia/Jakarta"),
        "Pekanbaru"        to CityInfo(0.51,   101.44, "Asia/Jakarta"),
        "Batam"            to CityInfo(1.13,   104.05, "Asia/Jakarta"),
        "Padang"           to CityInfo(-0.94,  100.35, "Asia/Jakarta"),
        "Jambi"            to CityInfo(-1.61,  103.61, "Asia/Jakarta"),
        "Palembang"        to CityInfo(-2.99,  104.75, "Asia/Jakarta"),
        "Bengkulu"         to CityInfo(-3.80,  102.27, "Asia/Jakarta"),
        "Bandar Lampung"   to CityInfo(-5.45,  105.26, "Asia/Jakarta"),
        "Pangkal Pinang"   to CityInfo(-2.13,  106.12, "Asia/Jakarta"),
        "Tanjung Pinang"   to CityInfo(0.92,   104.46, "Asia/Jakarta"),
        "Jakarta"          to CityInfo(-6.21,  106.85, "Asia/Jakarta"),
        "Bogor"            to CityInfo(-6.60,  106.80, "Asia/Jakarta"),
        "Bandung"          to CityInfo(-6.91,  107.61, "Asia/Jakarta"),
        "Serang"           to CityInfo(-6.12,  106.15, "Asia/Jakarta"),
        "Semarang"         to CityInfo(-6.97,  110.42, "Asia/Jakarta"),
        "Yogyakarta"       to CityInfo(-7.80,  110.36, "Asia/Jakarta"),
        "Solo"             to CityInfo(-7.57,  110.83, "Asia/Jakarta"),
        "Surabaya"         to CityInfo(-7.25,  112.75, "Asia/Jakarta"),
        "Malang"           to CityInfo(-7.98,  112.63, "Asia/Jakarta"),
        "Madiun"           to CityInfo(-7.63,  111.52, "Asia/Jakarta"),
        "Kediri"           to CityInfo(-7.82,  112.01, "Asia/Jakarta"),
        "Pontianak"        to CityInfo(-0.02,  109.33, "Asia/Jakarta"),
        "Palangkaraya"     to CityInfo(-2.21,  113.92, "Asia/Jakarta"),
        "Banjarmasin"      to CityInfo(-3.32,  114.59, "Asia/Makassar"),
        "Balikpapan"       to CityInfo(-1.27,  116.83, "Asia/Makassar"),
        "Samarinda"        to CityInfo(-0.50,  117.15, "Asia/Makassar"),
        "Tanjung Selor"    to CityInfo(2.84,   117.37, "Asia/Makassar"),
        "Nusantara (IKN)"  to CityInfo(-1.10,  116.71, "Asia/Makassar"),
        "Makassar"         to CityInfo(-5.14,  119.41, "Asia/Makassar"),
        "Palu"             to CityInfo(-0.90,  119.87, "Asia/Makassar"),
        "Kendari"          to CityInfo(-3.97,  122.51, "Asia/Makassar"),
        "Mamuju"           to CityInfo(-2.67,  118.89, "Asia/Makassar"),
        "Gorontalo"        to CityInfo(0.54,   123.06, "Asia/Makassar"),
        "Manado"           to CityInfo(1.47,   124.84, "Asia/Makassar"),
        "Denpasar"         to CityInfo(-8.65,  115.22, "Asia/Makassar"),
        "Mataram"          to CityInfo(-8.58,  116.10, "Asia/Makassar"),
        "Kupang"           to CityInfo(-10.17, 123.58, "Asia/Makassar"),
        "Ambon"            to CityInfo(-3.69,  128.18, "Asia/Jayapura"),
        "Sofifi"           to CityInfo(0.74,   127.56, "Asia/Jayapura"),
        "Jayapura"         to CityInfo(-2.53,  140.72, "Asia/Jayapura"),
        "Manokwari"        to CityInfo(-0.86,  134.08, "Asia/Jayapura"),
        "Sorong"           to CityInfo(-0.88,  131.25, "Asia/Jayapura"),
        "Merauke"          to CityInfo(-8.49,  140.40, "Asia/Jayapura"),
        "Nabire"           to CityInfo(-3.37,  135.50, "Asia/Jayapura")
    )

    init {
        // Tidak auto-fetch di init, biarkan MainActivity yang handle setelah permission
        // Tapi load cache dulu kalau ada
        val cached = weatherCache.load()
        if (cached != null) {
            _weatherState.value = UiState.Success(cached.bundle)
            _selectedCity.value = cached.cityName
            _selectedDistrict.value = cached.districtName
            _isGpsLocation.value = cached.isGpsLocation
            _cacheTimestamp.value = cached.timestamp
            _isFromCache.value = true
        }
    }

    // Dipanggil dari MainActivity setelah permission granted
    fun initWithGps() {
        viewModelScope.launch {
            _isDetectingLocation.value = true
            _selectedCity.value = "Mendeteksi lokasi..."
            _selectedDistrict.value = ""
            _weatherState.value = UiState.Loading

            val loc = locationManager.getCurrentLocation()
            _isDetectingLocation.value = false

            if (loc != null) {
                _selectedCity.value = loc.cityName
                _selectedDistrict.value = loc.districtName
                _isGpsLocation.value = true
                saveLastGpsLocation(loc.cityName, loc.districtName)
                fetchWeatherByCoords(loc.latitude, loc.longitude, loc.timezone)
            } else {
                val cached = weatherCache.load()
                if (cached != null) {
                    _weatherState.value = UiState.Success(cached.bundle)
                    _selectedCity.value = cached.cityName
                    _selectedDistrict.value = cached.districtName
                    _isGpsLocation.value = cached.isGpsLocation
                    _cacheTimestamp.value = cached.timestamp
                    _isFromCache.value = true
                } else {
                    _weatherState.value = UiState.Idle
                    _selectedCity.value = ""
                }
            }
        }
    }

    // Dipanggil kalau user tolak permission
    fun initWithoutGps() {
        setUsingGps(false)
        val cached = weatherCache.load()
        if (cached != null) {
            _weatherState.value = UiState.Success(cached.bundle)
            _selectedCity.value = cached.cityName
            _selectedDistrict.value = cached.districtName
            _isGpsLocation.value = cached.isGpsLocation
            _cacheTimestamp.value = cached.timestamp
            _isFromCache.value = true
        } else {
            _weatherState.value = UiState.Idle
        }
    }

    fun fetchWeather(cityName: String) {
        val city = cities[cityName] ?: return
        // User memilih manual → nonaktifkan GPS flag
        setUsingGps(false)
        _isGpsLocation.value = false
        _selectedCity.value = cityName
        _selectedDistrict.value = ""
        _weatherState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val forecastDeferred = async {
                    RetrofitInstance.weatherApi.getForecast(
                        latitude = city.lat,
                        longitude = city.lon,
                        timezone = city.timezone
                    )
                }
                val airQualityDeferred = async {
                    RetrofitInstance.airQualityApi.getAirQuality(
                        latitude = city.lat,
                        longitude = city.lon,
                        timezone = city.timezone
                    )
                }
                val bundle = WeatherBundle(
                    forecast = forecastDeferred.await(),
                    airQuality = airQualityDeferred.await()
                )
                _weatherState.value = UiState.Success(bundle)
                _isFromCache.value = false
                _cacheTimestamp.value = null

                // Simpan ke cache
                weatherCache.save(
                    CachedWeather(
                        bundle = bundle,
                        cityName = cityName,
                        districtName = "",
                        timestamp = System.currentTimeMillis(),
                        isGpsLocation = false
                    )
                )
            } catch (e: Exception) {
                // Coba fallback ke cache
                val cached = weatherCache.load()
                if (cached != null) {
                    _weatherState.value = UiState.Success(cached.bundle)
                    _cacheTimestamp.value = cached.timestamp
                    _isFromCache.value = true
                } else {
                    _weatherState.value = UiState.Error("Gagal mengambil data: ${e.message}")
                }
            }
        }
    }

    private suspend fun fetchWeatherByCoords(lat: Double, lon: Double, timezone: String) {
        try {
            val bundle = kotlinx.coroutines.coroutineScope {
                val forecastDeferred = async {
                    RetrofitInstance.weatherApi.getForecast(
                        latitude = lat,
                        longitude = lon,
                        timezone = timezone
                    )
                }
                val airQualityDeferred = async {
                    RetrofitInstance.airQualityApi.getAirQuality(
                        latitude = lat,
                        longitude = lon,
                        timezone = timezone
                    )
                }
                WeatherBundle(
                    forecast = forecastDeferred.await(),
                    airQuality = airQualityDeferred.await()
                )
            }
            _weatherState.value = UiState.Success(bundle)
            _isFromCache.value = false
            _cacheTimestamp.value = null

            weatherCache.save(
                CachedWeather(
                    bundle = bundle,
                    cityName = _selectedCity.value,
                    districtName = _selectedDistrict.value,
                    timestamp = System.currentTimeMillis(),
                    isGpsLocation = true
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("AtmoscopeDebug", "fetchWeatherByCoords FAILED: ${e.message}", e)
            val cached = weatherCache.load()
            if (cached != null) {
                _weatherState.value = UiState.Success(cached.bundle)
                _cacheTimestamp.value = cached.timestamp
                _isFromCache.value = true
            } else {
                _weatherState.value = UiState.Error("Gagal mengambil data: ${e.message}")
            }
        }
    }

    fun enableGpsMode() {
        setUsingGps(true)
        viewModelScope.launch {
            _isDetectingLocation.value = true
            _selectedCity.value = "Mendeteksi lokasi..."
            _selectedDistrict.value = ""
            _weatherState.value = UiState.Loading

            val loc = locationManager.getCurrentLocation()
            _isDetectingLocation.value = false

            if (loc != null) {
                _selectedCity.value = loc.cityName
                _selectedDistrict.value = loc.districtName
                _isGpsLocation.value = true
                saveLastGpsLocation(loc.cityName, loc.districtName)
                fetchWeatherByCoords(loc.latitude, loc.longitude, loc.timezone)
            } else {
                _weatherState.value = UiState.Error("Tidak dapat mendeteksi lokasi")
                _selectedCity.value = ""
            }
        }
    }

    private fun saveLastGpsLocation(city: String, district: String) {
        _lastGpsLocationName.value = city
        _lastGpsDistrictName.value = district
        prefs.edit()
            .putString("last_gps_city", city)
            .putString("last_gps_district", district)
            .apply()
    }

    fun addCity(name: String) {
        if (cities.containsKey(name)) {
            val current = _savedCities.value.toMutableList()
            if (!current.contains(name)) {
                current.add(name)
                _savedCities.value = current
                saveCitiesToPrefs(current)
            }
        }
    }

    fun removeCity(name: String) {
        val current = _savedCities.value.filter { it != name }
        _savedCities.value = current
        saveCitiesToPrefs(current)
    }

    fun toggleTheme() { _isDarkTheme.value = !_isDarkTheme.value }
    fun setTempUnit(unit: String) { _tempUnit.value = unit }
    fun toggleAstroMode() { _isAstroMode.value = !_isAstroMode.value }

    private fun setUsingGps(value: Boolean) {
        _isUsingGps.value = value
        prefs.edit().putBoolean("is_using_gps", value).apply()
    }

    fun getCacheAgeString(): String {
        val ts = _cacheTimestamp.value ?: return ""
        return weatherCache.getAgeString(ts)
    }

    fun convertTemp(celsius: Double): String {
        return when (_tempUnit.value) {
            "F" -> "%.1f°F".format(celsius * 9 / 5 + 32)
            "K" -> "%.1fK".format(celsius + 273.15)
            else -> "%.1f°C".format(celsius)
        }
    }

    private fun saveCitiesToPrefs(cities: List<String>) {
        prefs.edit().putString("saved_cities", gson.toJson(cities)).apply()
    }

    private fun loadSavedCities(): List<String> {
        val json = prefs.getString("saved_cities", null) ?: return listOf("Bandung")
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            listOf("Bandung")
        }
    }
}