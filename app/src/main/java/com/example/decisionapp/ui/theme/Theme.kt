package com.example.decisionapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Цветовая палитра приложения
val DecisionBlue = Color(0xFF2563EB)
val DecisionBlueDark = Color(0xFF1D4ED8)
val DecisionBlueLight = Color(0xFF60A5FA)
val DecisionTeal = Color(0xFF0D9488)
val DecisionSurface = Color(0xFFF8FAFC)
val DecisionSurfaceDark = Color(0xFF1E293B)
val DecisionGold = Color(0xFFF59E0B)
val DecisionGreen = Color(0xFF10B981)
val DecisionRed = Color(0xFFEF4444)
val DecisionPurple = Color(0xFF8B5CF6)

private val LightColorScheme = lightColorScheme(
    primary = DecisionBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = DecisionTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF134E4A),
    background = DecisionSurface,
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    error = DecisionRed,
    outline = Color(0xFFCBD5E1)
)

private val DarkColorScheme = darkColorScheme(
    primary = DecisionBlueLight,
    onPrimary = Color(0xFF1E3A8A),
    primaryContainer = DecisionBlueDark,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF134E4A),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = DecisionSurfaceDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    error = Color(0xFFFCA5A5),
    outline = Color(0xFF475569)
)

@Composable
fun DecisionAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}