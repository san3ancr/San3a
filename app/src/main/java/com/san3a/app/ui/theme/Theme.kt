package com.san3a.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Surface,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenDark,
    secondary = Amber,
    onSecondary = TextPrimary,
    secondaryContainer = AmberLight,
    onSecondaryContainer = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = GreenLight,
    onSurfaceVariant = TextSecondary,
    outline = Outline,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = DarkGreen,
    onPrimary = Color(0xFF00332B),
    primaryContainer = GreenDark,
    onPrimaryContainer = Surface,
    secondary = Amber,
    onSecondary = Color(0xFF3A2700),
    secondaryContainer = AmberLight,
    onSecondaryContainer = TextPrimary,
    background = DarkBackground,
    onBackground = Surface,
    surface = DarkSurface,
    onSurface = Surface,
    surfaceVariant = GreenDark,
    onSurfaceVariant = GreenLight,
    outline = TextSecondary,
    error = ErrorRed
)

@Composable
fun San3aTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = San3aTypography,
            content = content
        )
    }
}
