package com.example.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

// Equivalent to Flutter's ThemeData in Material 3
private val LexNovaDarkColorScheme = darkColorScheme(
    primary = LexNovaPurpleLight,
    onPrimary = LexNovaSlateDark,
    primaryContainer = LexNovaPurpleDark,
    onPrimaryContainer = LexNovaSlateLight,
    secondary = LexNovaAccent,
    background = LexNovaSlateDark,
    surface = LexNovaSlateMedium,
    onBackground = Color.White, // Crisp white for dark theme visibility
    onSurface = Color.White, // Crisp white for dark theme visibility
    error = LexNovaError
)

private val LexNovaLightColorScheme = lightColorScheme(
    primary = LexNovaPurple,
    onPrimary = LexNovaSlateLight,
    primaryContainer = LexNovaPurpleLight,
    onPrimaryContainer = LexNovaSlateDark,
    secondary = LexNovaAccent,
    background = LexNovaSlateLight,
    surface = Color.White,
    onBackground = Color.Black, // Force visibility for light theme
    onSurface = Color.Black, // Force visibility for light theme
    error = LexNovaError
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to enforce LexNova design system
    content: @Composable () -> Unit
) {
    // Force light color scheme — the CAMS design system uses hardcoded light colors
    // (white cards, CamsBackground, explicit Color.White surfaces) throughout all screens.
    // Dark theme causes invisible text and broken card backgrounds.
    val colorScheme = LexNovaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
