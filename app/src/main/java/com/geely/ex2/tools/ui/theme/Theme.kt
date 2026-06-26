package com.geely.ex2.tools.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = FlymeAccent,
    onPrimary = FlymeSurface,
    secondary = GeelyBlueLight,
    onSecondary = GeelySilver,
    background = GeelyEx2BackgroundLightBottom,
    surface = Color(0xFFFDFEFF),
    surfaceVariant = Color(0xFFF1F6FD),
    onSurface = Color(0xFF0F2235),
    onSurfaceVariant = Color(0xFF475A6D),
    outlineVariant = Color(0xFFD2DEEB),
)

private val DarkColorScheme = darkColorScheme(
    primary = GeelyBlueLight,
    onPrimary = GeelySilver,
    secondary = GeelyBlue,
    onSecondary = GeelySilver,
    background = GeelyEx2BackgroundDarkBottom,
    surface = Color(0xFF1A2633),
    surfaceVariant = Color(0xFF223243),
    onSurface = Color(0xFFEAF2FB),
    onSurfaceVariant = Color(0xFFB6C4D3),
    outlineVariant = Color(0xFF44586D),
)

@Composable
fun GeelyEx2ToolsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
