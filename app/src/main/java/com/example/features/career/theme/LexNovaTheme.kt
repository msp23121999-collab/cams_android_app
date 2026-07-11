package com.example.features.career.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LexNovaDarkBlue = Color(0xFF0B132B)
val LexNovaGold = Color(0xFFD4AF37)
val LexNovaSurface = Color(0xFF1C2541)
val LexNovaTextPrimary = androidx.compose.ui.graphics.Color.White // slate-300
val LexNovaTextSecondary = Color(0xFF94A3B8) // slate-400

private val LexNovaColorScheme = darkColorScheme(
    primary = LexNovaGold,
    onPrimary = LexNovaDarkBlue,
    background = LexNovaDarkBlue,
    onBackground = LexNovaTextPrimary,
    surface = LexNovaSurface,
    onSurface = LexNovaTextPrimary,
    secondary = Color(0xFF3A506B),
    onSecondary = Color.White
)

@Composable
fun LexNovaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LexNovaColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
