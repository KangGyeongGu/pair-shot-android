package com.pairshot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkPrimary,
        onPrimary = OnPrimary,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnBackground,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnBackgroundVariant,
        error = DarkError,
        onError = OnPrimary,
        outline = DarkDivider,
        outlineVariant = DarkDivider,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = LightPrimary,
        onPrimary = OnPrimary,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnBackground,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightOnBackgroundVariant,
        error = LightError,
        onError = OnPrimary,
        outline = LightDivider,
        outlineVariant = LightDivider,
    )

@Composable
fun PairShotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PairShotTypography,
        shapes = PairShotShapes,
        content = content,
    )
}
