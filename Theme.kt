package com.mahin.azu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = AzuBlueLight,
    background = AzuBackgroundDark,
    surface = AzuSurfaceDark,
    onBackground = AzuOnBackgroundDark,
    onSurface = AzuOnBackgroundDark,
)
private val LightColors = lightColorScheme(
    primary = AzuBlue,
    background = AzuBackgroundLight,
    surface = AzuSurfaceLight,
    onBackground = AzuOnBackgroundLight,
    onSurface = AzuOnBackgroundLight,
)

@Composable
fun AzuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
