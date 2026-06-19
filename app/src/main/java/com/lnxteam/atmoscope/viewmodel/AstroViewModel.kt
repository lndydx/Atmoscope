package com.lnxteam.atmoscope.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lnxteam.atmoscope.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AstroViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("atmoscope_prefs", Context.MODE_PRIVATE)
    private val apodCache = ApodCache(prefs)

    private val _apodState = MutableStateFlow<UiState<ApodResponse>>(UiState.Idle)
    val apodState: StateFlow<UiState<ApodResponse>> = _apodState

    private val _planetState = MutableStateFlow<List<PlanetInfo>>(emptyList())
    val planetState: StateFlow<List<PlanetInfo>> = _planetState

    private var apodLoaded = false
    private var planetsLoaded = false

    fun loadApod() {
        if (apodLoaded) return
        apodLoaded = true

        val cached = apodCache.loadToday()
        if (cached != null) {
            _apodState.value = UiState.Success(cached)
            return
        }

        _apodState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apodApi.getApod(NasaConfig.API_KEY)
                apodCache.save(response)
                _apodState.value = UiState.Success(response)
            } catch (e: Exception) {
                apodLoaded = false
                _apodState.value = UiState.Error("Gagal memuat APOD: ${e.message}")
            }
        }
    }

    fun calculatePlanets() {
        if (planetsLoaded) return
        planetsLoaded = true

        val lat = prefs.getFloat("last_lat", -6.91f).toDouble()
        val lon = prefs.getFloat("last_lon", 107.61f).toDouble()
        _planetState.value = PlanetCalculator.calculateAll(lat, lon)
    }
}