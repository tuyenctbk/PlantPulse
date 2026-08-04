package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = MintGreen,
    onPrimaryContainer = DeepEmerald,
    secondary = WaterBlue,
    onSecondary = Color.White,
    secondaryContainer = SoftBlueContainer,
    onSecondaryContainer = WaterBlue,
    tertiary = FertilizerPurple,
    background = SlateBackground,
    onBackground = CharcoalText,
    surface = SoftCardSurface,
    onSurface = CharcoalText,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = MutedSlate,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = MintGreen,
    onPrimary = DeepEmerald,
    primaryContainer = ForestGreen,
    onPrimaryContainer = LightMint,
    secondary = WaterBlue,
    onSecondary = Color.White,
    background = DarkForestBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurfaceCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF1E3A2F),
    onSurfaceVariant = DarkTextMuted,
    outline = Color(0xFF2D5041)
)

enum class ThemeMode(val label: String) {
    SYSTEM("System Auto 🌗"),
    LIGHT("Light Mode ☀️"),
    DARK("Dark Mode 🌙")
}

@Composable
fun PlantPulseTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
    dynamicColor: Boolean = false, // Keep consistent brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

