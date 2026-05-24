package com.example.atmoscope

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.atmoscope.ui.screens.CityManagementScreen
import com.example.atmoscope.ui.screens.MainWeatherScreen
import com.example.atmoscope.ui.screens.SettingsScreen
import com.example.atmoscope.ui.screens.SplashScreen
import com.example.atmoscope.ui.theme.AtmoscopeTheme
import com.example.atmoscope.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    private lateinit var weatherViewModel: WeatherViewModel

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            weatherViewModel.initWithGps()
        } else {
            weatherViewModel.initWithoutGps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: WeatherViewModel = viewModel()
            weatherViewModel = vm

            val isDark by vm.isDarkTheme.collectAsState()

            AtmoscopeTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(onFinish = {
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                            // Request permission setelah splash
                            locationPermissionRequest.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        })
                    }
                    composable("main") {
                        MainWeatherScreen(
                            viewModel = vm,
                            onNavigateToCityManagement = { navController.navigate("cities") },
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }
                    composable("cities") {
                        CityManagementScreen(
                            viewModel = vm,
                            onBack = { navController.popBackStack() },
                            onCitySelected = { city ->
                                vm.fetchWeather(city)
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = vm,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}