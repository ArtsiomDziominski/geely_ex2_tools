package com.geely.ex2.tools.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

@Composable
fun GeelyEx2Background(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val gradientColors = if (isDarkTheme) {
        listOf(
            GeelyEx2BackgroundDarkTop,
            GeelyEx2BackgroundDarkBottom,
        )
    } else {
        listOf(
            GeelyEx2BackgroundLightTop,
            GeelyEx2BackgroundLightBottom,
        )
    }
    val glowBlue = if (isDarkTheme) GeelyEx2GlowDarkBlue else GeelyEx2GlowLightBlue
    val glowMint = if (isDarkTheme) GeelyEx2GlowDarkMint else GeelyEx2GlowLightMint

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                ),
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowBlue,
                        glowBlue.copy(alpha = 0f),
                    ),
                    radius = size.minDimension * 0.7f,
                    center = Offset(size.width * 0.1f, size.height * 0.1f),
                ),
                radius = size.minDimension * 0.7f,
                center = Offset(size.width * 0.1f, size.height * 0.1f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowMint,
                        glowMint.copy(alpha = 0f),
                    ),
                    radius = size.minDimension * 0.65f,
                    center = Offset(size.width * 0.9f, size.height * 0.85f),
                ),
                radius = size.minDimension * 0.65f,
                center = Offset(size.width * 0.9f, size.height * 0.85f),
            )
        }
        content()
    }
}
