package com.example.duetmeal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandDark,
    onPrimary = Color.White,
    primaryContainer = BrandLight,
    onPrimaryContainer = BrandDark,
    secondary = BrandGold,
    onSecondary = Color.Black,
    background = SurfaceBg,
    onBackground = Ink,
    surface = CardBg,
    onSurface = Ink,
    surfaceVariant = SurfaceBg,
    onSurfaceVariant = Muted
)

@Composable
fun DUETMealTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
