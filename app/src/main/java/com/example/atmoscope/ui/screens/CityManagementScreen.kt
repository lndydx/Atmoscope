package com.example.atmoscope.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atmoscope.ui.theme.Purple
import com.example.atmoscope.ui.theme.PurpleLight
import com.example.atmoscope.viewmodel.WeatherViewModel
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityManagementScreen(
    viewModel: WeatherViewModel,
    onBack: () -> Unit,
    onCitySelected: (String) -> Unit
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val savedCities by viewModel.savedCities.collectAsState()
    val lastGpsCity by viewModel.lastGpsLocationName.collectAsState()
    val lastGpsDistrict by viewModel.lastGpsDistrictName.collectAsState()
    val allCities = viewModel.cities.keys.toList()

    val bgColor = if (isDark) Color(0xFF0D1117) else Color(0xFFE8EDF5)
    val cardColor = if (isDark) Color(0xFF161B22) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1A1F2E)
    val subTextColor = if (isDark) Color(0xFF8B949E) else Color(0xFF6B7280)

    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("City Management", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add City", tint = PurpleLight)
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── GPS Entry — selalu muncul di paling atas ──
            GpsLocationCard(
                lastCity = lastGpsCity,
                lastDistrict = lastGpsDistrict,
                cardColor = cardColor,
                textColor = textColor,
                subTextColor = subTextColor,
                onClick = {
                    viewModel.enableGpsMode()
                    onBack()
                }
            )

            if (savedCities.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No cities saved yet",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Tap + to add a city",
                            color = subTextColor,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(savedCities, key = { it }) { city ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInHorizontally() + fadeIn(),
                            exit = slideOutHorizontally() + fadeOut()
                        ) {
                            CityCard(
                                cityName = city,
                                cardColor = cardColor,
                                textColor = textColor,
                                subTextColor = subTextColor,
                                onClick = { onCitySelected(city) },
                                onDelete = { viewModel.removeCity(city) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = cardColor,
            title = {
                Text("Add City", fontWeight = FontWeight.Bold, color = textColor)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search city...", color = subTextColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple,
                            unfocusedBorderColor = subTextColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val filtered = allCities.filter {
                        it.contains(searchQuery, ignoreCase = true)
                    }.take(8)
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filtered) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addCity(city)
                                        showAddDialog = false
                                        searchQuery = ""
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = PurpleLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(city, color = textColor)
                            }
                            HorizontalDivider(color = subTextColor.copy(alpha = 0.2f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false; searchQuery = "" }) {
                    Text("Cancel", color = Purple)
                }
            }
        )
    }
}

// ── GPS Card ──────────────────────────────────────────────
@Composable
fun GpsLocationCard(
    lastCity: String,
    lastDistrict: String,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Purple.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, PurpleLight.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("📍", fontSize = 24.sp)
                Column {
                    Text(
                        "Lokasi GPS",
                        fontWeight = FontWeight.Bold,
                        color = PurpleLight,
                        fontSize = 16.sp
                    )
                    Text(
                        if (lastCity.isEmpty()) "Tap untuk deteksi lokasi otomatis"
                        else if (lastDistrict.isEmpty()) lastCity
                        else "$lastCity · $lastDistrict",
                        color = subTextColor,
                        fontSize = 12.sp
                    )
                }
            }
            Icon(
                Icons.Default.MyLocation,
                contentDescription = null,
                tint = PurpleLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CityCard(
    cityName: String,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PurpleLight)
                Column {
                    Text(cityName, fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                    Text("Tap to view weather", color = subTextColor, fontSize = 12.sp)
                }
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Remove $cityName?") },
            text = { Text("This city will be removed from your list.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showConfirm = false }) {
                    Text("Remove", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel", color = Purple)
                }
            }
        )
    }
}