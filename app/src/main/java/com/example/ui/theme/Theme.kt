package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MusicBubbleColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    primaryContainer = BurgundyMedium,
    onPrimaryContainer = Color.White,
    secondary = CrimsonAccent,
    onSecondary = Color.White,
    secondaryContainer = BurgundyDark,
    onSecondaryContainer = TextPrimary,
    tertiary = CrimsonGlow,
    onTertiary = Color.White,
    background = PitchBlack,
    onBackground = TextPrimary,
    surface = PitchDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceCardBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MusicBubbleColorScheme,
        typography = Typography,
        content = content
    )
}
