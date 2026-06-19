package com.lnxteam.atmoscope.ui.screens

import androidx.compose.ui.draw.alpha
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.lnxteam.atmoscope.data.*
import com.lnxteam.atmoscope.ui.theme.*
import com.lnxteam.atmoscope.viewmodel.UiState
import com.lnxteam.atmoscope.viewmodel.WeatherBundle
import com.lnxteam.atmoscope.viewmodel.WeatherViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.lnxteam.atmoscope.viewmodel.AuthViewModel
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lnxteam.atmoscope.viewmodel.AstroViewModel
import kotlinx.coroutines.launch

@Composable
fun MainWeatherScreen(
    viewModel: WeatherViewModel,
    authViewModel: AuthViewModel,
    onNavigateToCityManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val isAstroMode by viewModel.isAstroMode.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    val bgGradient = if (isAstroMode) {
        Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF0A0E1A)))
    } else {
        getWeatherGradient(weatherState, isDark)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        WeatherAnimatedBackground(weatherState, isAstroMode)

        if (isAstroMode) {
            AstroPage(
                viewModel = viewModel,
                weatherState = weatherState,
                onHamburger = onNavigateToCityManagement,
                onSettings = onNavigateToSettings,
                selectedCity = selectedCity
            )
        } else {
            WeatherModePager(
                viewModel = viewModel,
                weatherState = weatherState,
                onHamburger = onNavigateToCityManagement,
                onSettings = onNavigateToSettings,
                onAstroClick = if (authViewModel.isLoggedIn) {
                    { viewModel.toggleAstroMode() }
                } else null,
                onNavigateToLogin = onNavigateToLogin,
                selectedCity = selectedCity
            )
        }
    }
}

@Composable
fun MainWeatherScreen(
    viewModel: WeatherViewModel,
    authViewModel: AuthViewModel,
    onNavigateToCityManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val isAstroMode by viewModel.isAstroMode.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val astroViewModel: AstroViewModel = viewModel()

    val bgGradient = if (isAstroMode) {
        Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF0A0E1A)))
    } else {
        getWeatherGradient(weatherState, isDark)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        WeatherAnimatedBackground(weatherState, isAstroMode)

        if (isAstroMode) {
            AstroPage(
                viewModel = viewModel,
                astroViewModel = astroViewModel,
                weatherState = weatherState,
                onHamburger = onNavigateToCityManagement,
                onSettings = onNavigateToSettings,
                selectedCity = selectedCity
            )
        } else {
            WeatherModePager(
                viewModel = viewModel,
                weatherState = weatherState,
                onHamburger = onNavigateToCityManagement,
                onSettings = onNavigateToSettings,
                onAstroClick = if (authViewModel.isLoggedIn) {
                    { viewModel.toggleAstroMode() }
                } else null,
                onNavigateToLogin = onNavigateToLogin,
                selectedCity = selectedCity
            )
        }
    }
}

// ── WEATHER PAGE ──────────────────────────────────────────
@Composable
fun WeatherPage(
    viewModel: WeatherViewModel,
    weatherState: UiState<WeatherBundle>,
    onHamburger: () -> Unit,
    onSettings: () -> Unit,
    onAstroClick: (() -> Unit)?,
    onNavigateToLogin: () -> Unit,
    selectedCity: String
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        val blurAlpha = (scrollState.value / 1000f).coerceIn(0f, 0.5f)
        if (blurAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = blurAlpha))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            val isGpsLocation by viewModel.isGpsLocation.collectAsState()
            val isFromCache by viewModel.isFromCache.collectAsState()
            val selectedDistrict by viewModel.selectedDistrict.collectAsState()

            WeatherTopBar(
                city = selectedCity.ifEmpty { "Pilih Kota" },
                district = selectedDistrict,
                isGpsLocation = isGpsLocation,
                isFromCache = isFromCache,
                cacheAge = viewModel.getCacheAgeString(),
                onHamburger = onHamburger,
                onSettings = onSettings,
                onAstroClick = onAstroClick
            )

            when (weatherState) {
                is UiState.Idle -> IdlePrompt()
                is UiState.Loading -> LoadingIndicator()
                is UiState.Error -> ErrorCard(weatherState.message)
                is UiState.Success -> {
                    val bundle = weatherState.data
                    val forecast = bundle.forecast
                    val current = forecast.current
                    val currentAqi = bundle.airQuality.hourly.aqi
                        .firstOrNull { it != null }

                    HeroTemperature(
                        viewModel = viewModel,
                        current = current
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    SectionTitle("Next 48 Hours")
                    HourlyForecastRow(
                        viewModel = viewModel,
                        hourly = forecast.hourly,
                        timezone = forecast.timezone
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SectionTitle("7-Day Forecast")
                    SevenDayForecast(
                        viewModel = viewModel,
                        daily = forecast.daily
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SectionTitle("Conditions")
                    WeatherInfoGrid(
                        current = current,
                        daily = forecast.daily,
                        aqi = currentAqi
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ── LIFE INDEX PAGE ───────────────────────────────────────
@Composable
fun LifeIndexPage(
    viewModel: WeatherViewModel,
    weatherState: UiState<WeatherBundle>,
    onHamburger: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        WeatherTopBar(
            city = "Life Index",
            district = "",
            isGpsLocation = false,
            isFromCache = false,
            cacheAge = "",
            onHamburger = onHamburger,
            onSettings = onSettings
        )
        when (weatherState) {
            is UiState.Success -> {
                val current = weatherState.data.forecast.current
                LifeIndexContent(current)
            }
            is UiState.Idle -> IdlePrompt()
            is UiState.Loading -> LoadingIndicator()
            is UiState.Error -> ErrorCard(weatherState.message)
        }
    }
}

@Composable
fun LifeIndexContent(current: CurrentWeather) {
    val indices = getLifeIndices(current)
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        indices.forEach { index ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = index.color.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(index.icon, fontSize = 32.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            index.title,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            index.description,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { index.level },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = index.color,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

data class LifeIndex(
    val icon: String,
    val title: String,
    val description: String,
    val level: Float,
    val color: Color
)

fun getLifeIndices(current: CurrentWeather): List<LifeIndex> {
    val isRainy = current.precipitation > 0
    val isCloudy = current.cloudcover > 70
    val isHumid = current.humidity > 75
    val isWindy = current.windSpeed > 20
    val isClear = !isRainy && !isCloudy

    return listOf(
        LifeIndex(
            icon = "☀️",
            title = if (isClear) "UV Tinggi" else "UV Rendah",
            description = if (isClear) "Pakai sunscreen, hindari matahari siang" else "Radiasi UV rendah hari ini",
            level = if (isClear) 0.85f else 0.2f,
            color = if (isClear) Color(0xFFF59E0B) else Color(0xFF34D399)
        ),
        LifeIndex(
            icon = if (isHumid) "🥵" else "😊",
            title = if (isHumid) "Panas & Lembab" else "Nyaman",
            description = if (isHumid) "Pakai pakaian tipis & ringan" else "Kondisi nyaman hari ini",
            level = if (isHumid) 0.8f else 0.3f,
            color = if (isHumid) Color(0xFFEF4444) else Color(0xFF34D399)
        ),
        LifeIndex(
            icon = if (isRainy) "🤧" else "💪",
            title = if (isRainy) "Risiko Masuk Angin" else "Cuaca Stabil",
            description = if (isRainy) "Hujan meningkatkan risiko masuk angin" else "Kecil kemungkinan sakit",
            level = if (isRainy) 0.7f else 0.15f,
            color = if (isRainy) Color(0xFF60A5FA) else Color(0xFF34D399)
        ),
        LifeIndex(
            icon = if (isHumid) "🦟" else "✅",
            title = if (isHumid) "Risiko Demam Berdarah Tinggi" else "Risiko Demam Berdarah Rendah",
            description = if (isHumid) "Lembab meningkatkan aktivitas nyamuk" else "Kondisi kurang mendukung nyamuk",
            level = if (isHumid) 0.75f else 0.2f,
            color = if (isHumid) Color(0xFFEF4444) else Color(0xFF34D399)
        ),
        LifeIndex(
            icon = if (isWindy) "💨" else "🌬️",
            title = if (isWindy) "Angin Kencang" else "Angin Sepoi",
            description = if (isWindy) "Hindari aktivitas luar, amankan barang" else "Kondisi angin nyaman",
            level = (current.windSpeed / 50.0).toFloat().coerceIn(0f, 1f),
            color = if (isWindy) Color(0xFFA78BFA) else Color(0xFF34D399)
        ),
        LifeIndex(
            icon = if (isRainy) "☂️" else "👟",
            title = if (isRainy) "Bawa Payung" else "Cocok untuk Outdoor",
            description = if (isRainy) "Hujan diprediksi, jangan lupa payung" else "Hari yang bagus untuk aktivitas luar",
            level = if (isRainy) 1f else 0.1f,
            color = if (isRainy) Color(0xFF60A5FA) else Color(0xFF34D399)
        )
    )
}

// ── ASTRO PAGE ────────────────────────────────────────────
// ── ASTRO PAGE ────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AstroPage(
    viewModel: WeatherViewModel,
    astroViewModel: AstroViewModel,
    weatherState: UiState<WeatherBundle>,
    onHamburger: () -> Unit,
    onSettings: () -> Unit,
    selectedCity: String
) {
    val tabs = listOf("Overview", "Events", "APOD", "Planets", "Constellations", "Calculator")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    val isGpsLocation by viewModel.isGpsLocation.collectAsState()
    val isFromCache by viewModel.isFromCache.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        WeatherTopBar(
            city = selectedCity.ifEmpty { "Pilih Kota" },
            district = selectedDistrict,
            isGpsLocation = isGpsLocation,
            isFromCache = isFromCache,
            cacheAge = viewModel.getCacheAgeString(),
            onHamburger = onHamburger,
            onSettings = onSettings,
            onAstroClick = { viewModel.toggleAstroMode() }
        )

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = PurpleLight,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> AstroMainPage(viewModel = viewModel, weatherState = weatherState)
                1 -> AstroEventsPage()
                2 -> AstroApodPage(astroViewModel)
                3 -> AstroPlanetPage(astroViewModel)
                4 -> AstroConstellationPage()
                5 -> AstroCalculatorPage()
            }
        }
    }
}

@Composable
fun AstroMainPage(
    viewModel: WeatherViewModel,
    weatherState: UiState<WeatherBundle>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        when (weatherState) {
            is UiState.Success -> {
                val forecast = weatherState.data.forecast
                val aqi = weatherState.data.airQuality.hourly.aqi.firstOrNull { it != null }
                val skyScore = calculateSkyScore(forecast.current.cloudcover, aqi)

                AstroHero(skyScore = skyScore, cloudcover = forecast.current.cloudcover)
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Sky Score Forecast")
                SkyScoreChart(hourly = forecast.hourly, airQuality = weatherState.data.airQuality)
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Moon Phase")
                MoonPhaseCard(skyScore = skyScore)
                Spacer(modifier = Modifier.height(32.dp))
            }
            is UiState.Idle -> IdlePrompt()
            is UiState.Loading -> LoadingIndicator()
            is UiState.Error -> ErrorCard(weatherState.message)
        }
    }
}

@Composable
fun AstroHero(skyScore: Int, cloudcover: Int) {
    val scoreColor = when {
        skyScore >= 70 -> Color(0xFF34D399)
        skyScore >= 40 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
    val scoreLabel = when {
        skyScore >= 70 -> "Kondisi Sangat Baik"
        skyScore >= 40 -> "Kondisi Sedang"
        else -> "Visibilitas Buruk"
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text("🔭", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Sky Score", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Text(
            if (skyScore == 0) "< 1" else "$skyScore",
            fontSize = 72.sp,
            fontWeight = FontWeight.ExtraBold,
            color = scoreColor
        )
        Text(scoreLabel, color = scoreColor, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Awan: $cloudcover%",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
    }
}

@Composable
fun SkyScoreChart(hourly: HourlyWeather, airQuality: AirQualityResponse) {
    val nightItems = hourly.time.indices.take(24).toList()
    val scores = nightItems.map { i ->
        val aqi = airQuality.hourly.aqi.getOrNull(i)
        calculateSkyScore(hourly.cloudcover[i], aqi)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Y axis
                Column(
                    modifier = Modifier
                        .width(32.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("100", "80", "60", "40", "20", "0").forEach { label ->
                        Text(
                            label,
                            color = Color(0xFF8B949E),
                            fontSize = 9.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Scrollable bars
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(nightItems.size) { idx ->
                            val i = nightItems[idx]
                            val barFraction = scores[idx] / 100f
                            val barColor = when {
                                scores[idx] >= 70 -> Color(0xFF34D399)
                                scores[idx] >= 40 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .width(28.dp)
                                    .fillMaxHeight()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(barFraction)
                                            .align(Alignment.BottomCenter)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(barColor, barColor.copy(alpha = 0.5f))
                                                )
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    hourly.time[i].substring(11, 16),
                                    color = Color(0xFF8B949E),
                                    fontSize = 7.sp
                                )
                            }
                        }
                    }
                }
            }

            // Legend
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                listOf(
                    Pair(Color(0xFF34D399), "Bagus (≥70)"),
                    Pair(Color(0xFFF59E0B), "Sedang (40-69)"),
                    Pair(Color(0xFFEF4444), "Buruk (<40)")
                ).forEach { (color, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Text(label, color = Color(0xFF8B949E), fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MoonPhaseCard(skyScore: Int) {
    val dayOfYear = java.time.LocalDate.now().dayOfYear
    val year = java.time.LocalDate.now().year
    val totalDays = (year - 2000) * 365.25 + dayOfYear
    val moonCycle = 29.53 // days
    val phase = ((totalDays % moonCycle) / moonCycle * 100).toInt()

    val (phaseName, phaseEmoji) = when {
        phase < 3 || phase > 97 -> Pair("New Moon", "🌑")
        phase < 25 -> Pair("Waxing Crescent", "🌒")
        phase < 30 -> Pair("First Quarter", "🌓")
        phase < 47 -> Pair("Waxing Gibbous", "🌔")
        phase < 53 -> Pair("Full Moon", "🌕")
        phase < 72 -> Pair("Waning Gibbous", "🌖")
        phase < 75 -> Pair("Last Quarter", "🌗")
        else -> Pair("Waning Crescent", "🌘")
    }

    val effect = when {
        phase in 47..53 -> "Bulan terang — mungkin mempengaruhi pengamatan"
        phase in 30..47 || phase in 53..72 -> "Efek sedang pada pengamatan"
        else -> "Kondisi baik untuk pengamatan bintang"
    }

    val illumination = when {
        phase < 50 -> phase * 2
        else -> (100 - phase) * 2
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(phaseEmoji, fontSize = 52.sp)
            Column {
                Text(phaseName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(
                    "Illumination: ~$illumination%",
                    color = Color(0xFF8B949E),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(effect, color = Color(0xFF8B949E), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AstroEventsPage() {
    val events = listOf(
        Triple("🌠", "Lyrids Meteor Shower", "21–22 Apr 2026 · ~18 meteor/jam. Bulan sabit tipis, kondisi ideal."),
        Triple("🌠", "Eta Aquariids", "5–6 Mei 2026 · Hingga 50 meteor/jam. Puing dari Komet Halley."),
        Triple("🌕", "Gerhana Bulan Total", "26 Jun 2026 · Terlihat dari Indonesia. Blood Moon."),
        Triple("🪐", "Saturnus di Oposisi", "21 Jul 2026 · Terdekat ke Bumi. Terbaik lihat cincinnya."),
        Triple("🌠", "Perseids Meteor Shower", "12–13 Ags 2026 · Hingga 100 meteor/jam."),
        Triple("🌑", "Gerhana Bulan Penumbra", "28 Ags 2026 · Terlihat dari Indonesia."),
        Triple("🔵", "Neptunus di Oposisi", "16 Sep 2026 · Butuh teleskop. Tampak biru samar."),
        Triple("🌠", "Orionids", "21–22 Okt 2026 · Meteor cepat & terang dari Komet Halley."),
        Triple("🌠", "Geminids", "13–14 Des 2026 · Hingga 120 meteor/jam. Berwarna-warni.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        events.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { (icon, title, desc) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(icon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(desc, color = Color(0xFF8B949E), fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ── SHARED COMPONENTS ─────────────────────────────────────
@Composable
fun WeatherTopBar(
    city: String,
    district: String,
    isGpsLocation: Boolean,
    isFromCache: Boolean,
    cacheAge: String,
    onHamburger: () -> Unit,
    onSettings: () -> Unit,
    onAstroClick: (() -> Unit)? = null,
    onNavigateToLogin: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isGpsLocation) {
                        Text("📍", fontSize = 14.sp)
                    }
                    Text(
                        city.ifEmpty { "Pilih Kota" },
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
                if (district.isNotEmpty()) {
                    Text(
                        district,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
                if (isFromCache && cacheAge.isNotEmpty()) {
                    Text(
                        "Data terakhir diperbarui: $cacheAge",
                        color = Color(0xFFF59E0B).copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
            Row {
                IconButton(onClick = onHamburger) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
                if (onAstroClick != null) {
                    IconButton(onClick = { onAstroClick() }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Astronomy Mode", tint = PurpleLight)
                    }
                } else {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Login", tint = Color.White.copy(alpha = 0.5f))
                    }
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun HeroTemperature(viewModel: WeatherViewModel, current: CurrentWeather) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(current.weatherCode.toLottieAsset().toWeatherEmoji(), fontSize = 64.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            viewModel.convertTemp(current.temperature),
            fontSize = 72.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            current.weatherCode.toWeatherLabel(),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Terasa ${viewModel.convertTemp(current.feelsLike)}  •  ${current.windSpeed.toInt()} km/h",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
    }
}

@Composable
fun HourlyForecastRow(viewModel: WeatherViewModel, hourly: HourlyWeather, timezone: String) {
    val zoneId = try {
        java.time.ZoneId.of(timezone)
    } catch (e: Exception) {
        java.time.ZoneId.systemDefault()
    }
    val now = java.time.ZonedDateTime.now(zoneId)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    val startIndex = hourly.time.indexOfFirst {
        try {
            val t = LocalDateTime.parse(it.substring(0, 16), formatter).atZone(zoneId)
            !t.isBefore(now)
        } catch (e: Exception) { false }
    }.coerceAtLeast(0)

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(hourly.time.indices.drop(startIndex).take(48).toList()) { i ->
            val hour = hourly.time[i].substring(11, 16)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(hour, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(hourly.weatherCode[i].toLottieAsset().toWeatherEmoji(), fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        viewModel.convertTemp(hourly.temperature[i]),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SevenDayForecast(viewModel: WeatherViewModel, daily: DailyWeather) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            daily.time.indices.take(7).forEach { i ->
                val dayName = try {
                    val dt = LocalDateTime.parse(daily.time[i] + "T00:00")
                    dt.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("id"))
                } catch (e: Exception) { daily.time[i] }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dayName, color = Color.White, modifier = Modifier.width(48.dp))
                    Text(daily.weatherCode[i].toLottieAsset().toWeatherEmoji(), fontSize = 20.sp)
                    Text(
                        viewModel.convertTemp(daily.tempMax[i]),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "💧 ${daily.precipitationSum[i].toInt()}mm",
                        color = Blue,
                        fontSize = 12.sp
                    )
                }
                if (i < 6) HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
fun WeatherInfoGrid(current: CurrentWeather, daily: DailyWeather, aqi: Int?) {
    val aqiLabel = when {
        aqi == null -> "N/A"
        aqi < 20 -> "Sangat Baik"
        aqi < 40 -> "Baik"
        aqi < 60 -> "Sedang"
        aqi < 80 -> "Buruk"
        else -> "Sangat Buruk"
    }
    val uvLabel = when {
        daily.uvIndex.isEmpty() -> "N/A"
        daily.uvIndex[0] < 3 -> "Rendah"
        daily.uvIndex[0] < 6 -> "Sedang"
        daily.uvIndex[0] < 8 -> "Tinggi"
        else -> "Sangat Tinggi"
    }
    val sunrise = daily.sunrise.firstOrNull()?.substring(11, 16) ?: "--:--"
    val sunset = daily.sunset.firstOrNull()?.substring(11, 16) ?: "--:--"

    val cards = listOf(
        Triple(Icons.Default.WaterDrop, "Kelembaban", "${current.humidity}%"),
        Triple(Icons.Default.Air, "Kecepatan Angin", "${current.windSpeed.toInt()} km/h"),
        Triple(Icons.Default.Umbrella, "Presipitasi", "${current.precipitation} mm"),
        Triple(Icons.Default.Cloud, "Kualitas Udara", aqiLabel),
        Triple(Icons.Default.WbSunny, "Indeks UV", uvLabel),
        Triple(Icons.Default.NightsStay, "Matahari", "↑$sunrise ↓$sunset")
    )

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        cards.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (icon, label, value) ->
                    InfoCard(icon = icon, label = label, value = value, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun InfoCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = PurpleLight, modifier = Modifier.size(20.dp))
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun IdlePrompt() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🌍", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Pilih kota untuk memulai", color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center, fontSize = 16.sp)
            Text("Tap ☰ untuk kelola kota", color = Color.White.copy(alpha = 0.4f), textAlign = TextAlign.Center, fontSize = 13.sp)
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PurpleLight)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Mengambil data cuaca...", color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3A1A1A)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", fontSize = 24.sp)
            Column {
                Text("Gagal Memuat Data", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                Text(message, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            }
        }
    }
}

// ── ANIMATIONS ────────────────────────────────────────────
@Composable
fun WeatherAnimatedBackground(weatherState: UiState<WeatherBundle>, isAstroMode: Boolean) {
    if (isAstroMode) { StarfieldBackground(); return }
    if (weatherState is UiState.Success) {
        val current = weatherState.data.forecast.current
        when {
            current.precipitation > 5 -> RainAnimation()
            current.cloudcover > 70 -> CloudAnimation()
            else -> StarfieldBackground()
        }
    }
}

@Composable
fun StarfieldBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "twinkle"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stars = listOf(
            Pair(0.1f, 0.05f), Pair(0.3f, 0.12f), Pair(0.55f, 0.08f),
            Pair(0.7f, 0.18f), Pair(0.85f, 0.06f), Pair(0.2f, 0.25f),
            Pair(0.45f, 0.3f), Pair(0.65f, 0.22f), Pair(0.9f, 0.28f),
            Pair(0.15f, 0.4f), Pair(0.5f, 0.45f), Pair(0.78f, 0.38f)
        )
        stars.forEachIndexed { i, (xF, yF) ->
            drawCircle(
                color = Color.White.copy(alpha = if (i % 2 == 0) twinkle else 1f - twinkle + 0.3f),
                radius = if (i % 3 == 0) 3f else 2f,
                center = androidx.compose.ui.geometry.Offset(xF * size.width, yF * size.height)
            )
        }
    }
}

@Composable
fun RainAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "rain_offset"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(60) { i ->
            val x = (i * 47 % size.width.toInt()).toFloat()
            val y = ((offset * size.height + i * 83) % size.height)
            drawLine(
                color = Color(0xFF90CAF9).copy(alpha = 0.4f),
                start = androidx.compose.ui.geometry.Offset(x, y),
                end = androidx.compose.ui.geometry.Offset(x - 4f, y + 20f),
                strokeWidth = 1.5f
            )
        }
    }
}

@Composable
fun CloudAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "cloud")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "cloud_offset"
    )
    Box(modifier = Modifier.fillMaxSize()) {
        listOf(
            Triple(0.1f, 0.1f, 0.3f),
            Triple(0.5f, 0.05f, 0.25f),
            Triple(0.7f, 0.15f, 0.2f)
        ).forEach { (xFrac, yFrac, cloudAlpha) ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)
                    .offset(x = (xFrac * 300 + offset).dp, y = (yFrac * 200).dp)
            ) {
                Text("☁️", fontSize = 48.sp, modifier = Modifier.alpha(cloudAlpha))
            }
        }
    }
}

// ── HELPERS ───────────────────────────────────────────────
fun String.toWeatherEmoji(): String = when (this) {
    "lottie_sunny.json" -> "☀️"
    "lottie_partly_cloudy.json" -> "⛅"
    "lottie_cloudy.json" -> "☁️"
    "lottie_fog.json" -> "🌫️"
    "lottie_rain.json" -> "🌧️"
    "lottie_thunder.json" -> "⛈️"
    else -> "🌤️"
}

fun getWeatherGradient(weatherState: UiState<WeatherBundle>, isDark: Boolean): Brush {
    if (weatherState !is UiState.Success) {
        return Brush.verticalGradient(listOf(Color(0xFF0A0E1A), Color(0xFF0D1117)))
    }
    val current = weatherState.data.forecast.current
    val hour = try { current.time.substring(11, 13).toInt() } catch (e: Exception) { 12 }

    return when {
        current.precipitation > 5 -> Brush.verticalGradient(
            listOf(Color(0xFF1A1A2E), Color(0xFF2D3561))
        )
        current.precipitation > 0 -> Brush.verticalGradient(
            listOf(Color(0xFF37474F), Color(0xFF546E7A))
        )
        current.cloudcover > 70 -> Brush.verticalGradient(
            listOf(Color(0xFF607D8B), Color(0xFF90A4AE))
        )
        hour in 5..9 -> Brush.verticalGradient(
            listOf(Color(0xFFFFD059), Color(0xFF82D2F2))
        )
        hour in 10..16 -> Brush.verticalGradient(
            listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
        )
        hour in 17..18 -> Brush.verticalGradient(
            listOf(Color(0xFF4A2040), Color(0xFFB05A3A))
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF000000), Color(0xFF0D1117))
        )
    }
}