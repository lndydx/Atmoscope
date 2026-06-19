package com.lnxteam.atmoscope.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple = Color(0xFF7C3AED)
val PurpleLight = Color(0xFFA78BFA)
val Blue = Color(0xFF60A5FA)
val Teal = Color(0xFF34D399)
val Gold = Color(0xFFF59E0B)
val Red = Color(0xFFEF4444)
val DarkBg = Color(0xFF0D1117)
val DarkSurface = Color(0xFF161B22)
val DarkCard = Color(0xFF21262D)

private val DarkColors = darkColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    secondary = PurpleLight,
    onSecondary = Color.White,
    background = DarkBg,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFF8B949E),
)

private val LightColors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    secondary = PurpleLight,
    onSecondary = Color.White,
    background = Color(0xFFE8EDF5),
    onBackground = Color(0xFF1A1F2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1F2E),
    surfaceVariant = Color(0xFFE8EDF5),
    onSurfaceVariant = Color(0xFF6B7280),
)

@Composable
fun AtmoscopeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}