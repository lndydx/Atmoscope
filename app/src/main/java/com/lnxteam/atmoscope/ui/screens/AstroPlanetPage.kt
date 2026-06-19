package com.lnxteam.atmoscope.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lnxteam.atmoscope.data.PlanetInfo
import com.lnxteam.atmoscope.viewmodel.AstroViewModel
import kotlin.math.roundToInt

@Composable
fun AstroPlanetPage(astroViewModel: AstroViewModel) {
    val planets by astroViewModel.planetState.collectAsState()

    LaunchedEffect(Unit) { astroViewModel.calculatePlanets() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Visibilitas planet malam ini berdasarkan lokasi kamu",
            color = Color(0xFF8B949E),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (planets.isEmpty()) {
            LoadingIndicator()
        } else {
            planets.forEach { p -> PlanetCard(p) }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PlanetCard(planet: PlanetInfo) {
    val statusColor = if (planet.isVisible) Color(0xFF34D399) else Color(0xFF8B949E)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(planet.emoji, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(planet.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (planet.isVisible) "VISIBLE" else "NOT VISIBLE",
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(planet.description, color = Color(0xFF8B949E), fontSize = 12.sp)
                Text(
                    "Altitude: ${planet.altitude.roundToInt()}° · Azimuth: ${planet.azimuth.roundToInt()}°",
                    color = Color(0xFF8B949E),
                    fontSize = 11.sp
                )
            }
        }
    }
}