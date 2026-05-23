package com.example.atmoscope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.atmoscope.ui.screens.CityManagementScreen
import com.example.atmoscope.ui.screens.LoginScreen
import com.example.atmoscope.ui.screens.MainWeatherScreen
import com.example.atmoscope.ui.screens.SettingsScreen
import com.example.atmoscope.ui.screens.SplashScreen
import com.example.atmoscope.ui.theme.AtmoscopeTheme
import com.example.atmoscope.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WeatherViewModel = viewModel()
            val isDark by viewModel.isDarkTheme.collectAsState()

            AtmoscopeTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(onFinish = {
                            // Berubah: Setelah splash selesai, arahkan ke login
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }

                    // Baru: Halaman Login
                    composable("login") {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                // Sementara langsung arahkan ke main weather screen
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onRegisterClick = {
                                // TODO: Nanti tambahkan rute register di sini
                            }
                        )
                    }

                    composable("main") {
                        MainWeatherScreen(
                            viewModel = viewModel,
                            onNavigateToCityManagement = {
                                navController.navigate("cities")
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable("cities") {
                        CityManagementScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onCitySelected = { city ->
                                viewModel.fetchWeather(city)
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}