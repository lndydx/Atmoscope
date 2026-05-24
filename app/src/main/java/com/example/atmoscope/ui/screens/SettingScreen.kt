package com.example.atmoscope.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atmoscope.ui.theme.Purple
import com.example.atmoscope.ui.theme.PurpleLight
import com.example.atmoscope.viewmodel.WeatherViewModel
import com.example.atmoscope.viewmodel.AuthViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WeatherViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val tempUnit by viewModel.tempUnit.collectAsState()
    val isUsingGps by viewModel.isUsingGps.collectAsState()
    val notifEnabled by viewModel.notifEnabled.collectAsState()
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoggedIn = currentUser != null

    val bgColor = if (isDark) Color(0xFF0D1117) else Color(0xFFE8EDF5)
    val cardColor = if (isDark) Color(0xFF161B22) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1A1F2E)
    val subTextColor = if (isDark) Color(0xFF8B949E) else Color(0xFF6B7280)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.Bold, color = textColor)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardColor)
            )
        },
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Temperature Unit
            SettingCard(cardColor = cardColor) {
                Text(
                    "Temperature Unit",
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("C", "F", "K").forEach { unit ->
                        val label = when (unit) {
                            "C" -> "°C Celsius"
                            "F" -> "°F Fahrenheit"
                            else -> "K Kelvin"
                        }
                        FilterChip(
                            selected = tempUnit == unit,
                            onClick = { viewModel.setTempUnit(unit) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Theme
            SettingCard(cardColor = cardColor) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = PurpleLight
                        )
                        Column {
                            Text("Theme", fontWeight = FontWeight.Bold, color = textColor)
                            Text(
                                if (isDark) "Dark Mode" else "Light Mode",
                                color = subTextColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Purple
                        )
                    )
                }
            }

            // GPS
            SettingCard(cardColor = cardColor) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PurpleLight
                        )
                        Column {
                            Text(
                                "Lokasi Otomatis (GPS)",
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                if (isUsingGps) "Aktif — GPS"
                                else "Nonaktif — pilih kota manual",
                                color = subTextColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Switch(
                        checked = isUsingGps,
                        onCheckedChange = { enabled ->
                            if (enabled) viewModel.enableGpsMode()
                            else viewModel.fetchWeather(
                                viewModel.selectedCity.value.ifEmpty { "Bandung" }
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Purple
                        )
                    )
                }
            }

            // Notifikasi
            SettingCard(cardColor = cardColor) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = PurpleLight
                        )
                        Column {
                            Text(
                                "Notifikasi Cuaca Ekstrem",
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                if (notifEnabled) "Aktif — notif setiap pukul 06.00"
                                else "Nonaktif",
                                color = subTextColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Switch(
                        checked = notifEnabled,
                        onCheckedChange = { viewModel.toggleNotification(context) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Purple
                        )
                    )
                }
            }

            // Account
            SettingCard(cardColor = cardColor) {
                Text("Astronomy Account", fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                if (isLoggedIn) {
                    val displayName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Pengguna"
                    val email = currentUser?.email ?: "-"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Purple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                displayName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Column {
                            Text(displayName, fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                            Text(email, color = subTextColor, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    var showDeleteDialog by remember { mutableStateOf(false) }

                    OutlinedButton(
                        onClick = {
                            authViewModel.logout()
                            if (viewModel.isAstroMode.value) viewModel.toggleAstroMode()
                            onLoggedOut()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleLight.copy(alpha = 0.5f)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                    ) {
                        Text("Logout")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hapus Akun", color = Color(0xFFEF4444), fontSize = 13.sp)
                    }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            containerColor = cardColor,
                            title = { Text("Hapus Akun?", color = textColor, fontWeight = FontWeight.Bold) },
                            text = { Text("Akun kamu akan dihapus permanen.", color = subTextColor) },
                            confirmButton = {
                                TextButton(onClick = {
                                    authViewModel.deleteAccount {
                                        if (viewModel.isAstroMode.value) viewModel.toggleAstroMode()
                                        onLoggedOut()
                                    }
                                    showDeleteDialog = false
                                }) {
                                    Text("Hapus", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Batal", color = Purple)
                                }
                            }
                        )
                    }

                } else {
                    Text(
                        "Login untuk akses Astronomy Mode",
                        color = subTextColor,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login / Daftar")
                    }
                }
            }

            // About
            SettingCard(cardColor = cardColor) {
                Text("About", fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Atmoscope v1.0", color = subTextColor, fontSize = 13.sp)
                Text("Weather & Astronomy App", color = subTextColor, fontSize = 13.sp)
                Text("Powered by Open-Meteo", color = subTextColor, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun SettingCard(
    cardColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}