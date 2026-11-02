package com.elysium.orbithop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette « espace » (calquée sur l'icône du jeu)
val CosmicNavy = Color(0xFF101B3C)
val CosmicNavyDeep = Color(0xFF0B142E)
val OrbitBlue = Color(0xFF2C4192)
val PlanetOrange = Color(0xFFF4714E)
val PlanetOrangeDark = Color(0xFFE05B3E)
val SatelliteCream = Color(0xFFF7F3E8)
val PanelTeal = Color(0xFF2ED3C0)
val StarYellow = Color(0xFFFFD166)
val TextPrimary = Color(0xFFF7F3E8)
val TextSecondary = Color(0xFFA9B4D0)
val DangerRed = Color(0xFFE05B3E)

private val OrbitColorScheme = darkColorScheme(
    primary = PanelTeal,
    onPrimary = CosmicNavyDeep,
    secondary = PlanetOrange,
    onSecondary = CosmicNavyDeep,
    background = CosmicNavy,
    onBackground = TextPrimary,
    surface = CosmicNavyDeep,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF1B2A55),
    onSurfaceVariant = TextSecondary,
    error = DangerRed
)

@Composable
fun OrbitHopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OrbitColorScheme,
        typography = Typography(),
        content = content
    )
}
