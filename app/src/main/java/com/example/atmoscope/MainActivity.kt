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
import com.example.atmoscope.ui.screens.*
import com.example.atmoscope.ui.theme.AtmoscopeTheme
import com.example.atmoscope.viewmodel.AuthViewModel
import com.example.atmoscope.viewmodel.WeatherViewModel
import android.content.pm.PackageManager
import android.os.Build
import com.example.atmoscope.notification.NotificationScheduler

class MainActivity : ComponentActivity() {

    private lateinit var weatherViewModel: WeatherViewModel

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) weatherViewModel.initWithGps()
        else weatherViewModel.initWithoutGps()
    }

    private val notifPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: WeatherViewModel = viewModel()
            val authVm: AuthViewModel = viewModel()
            weatherViewModel = vm

            val isDark by vm.isDarkTheme.collectAsState()

            AtmoscopeTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash") {

                    composable("splash") {
                        SplashScreen(onFinish = {
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                                    != PackageManager.PERMISSION_GRANTED) {
                                    notifPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    locationPermissionRequest.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            } else {
                                locationPermissionRequest.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        })
                    }

                    composable("main") {
                        MainWeatherScreen(
                            viewModel = vm,
                            authViewModel = authVm,
                            onNavigateToCityManagement = { navController.navigate("cities") },
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToLogin = { navController.navigate("login") }
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
                            authViewModel = authVm,
                            onBack = { navController.popBackStack() },
                            onLoggedOut = { navController.popBackStack() },
                            onNavigateToLogin = { navController.navigate("login") }
                        )
                    }

                    composable("login") {
                        LoginScreen(
                            authViewModel = authVm,
                            onLoginSuccess = {
                                vm.toggleAstroMode()
                                navController.popBackStack()
                            },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            authViewModel = authVm,
                            onRegisterSuccess = {
                                vm.toggleAstroMode()
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }


                }
            }
        }
        NotificationScheduler.schedule(this)
    }
}