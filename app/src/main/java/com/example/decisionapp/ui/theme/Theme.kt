package com.example.decisionapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// === Новая палитра «Спокойный выбор» ===
val CalmBlue         = Color(0xFF6B96BE)
val CalmBlueDark     = Color(0xFF4A7AA8)
val CalmBlueLight    = Color(0xFFB5CEEA)
val CalmBlueSurface  = Color(0xFFE8F0F8)
val SageGreen        = Color(0xFF5BA89A)
val SageGreenDark    = Color(0xFF3D7A68)
val SageGreenLight   = Color(0xFFB0D8CF)
val SageGreenSurface = Color(0xFFE6F2EE)
val WarmAmber        = Color(0xFFFACB3E)
val WarmAmberSurface = Color(0xFFF5EFE2)
val WarmBg           = Color(0xFFF5F3EE)
val WarmCard         = Color(0xFFFFFFFF)
val WarmBorder       = Color(0xFFE8E5DF)
val TextPrimary      = Color(0xFF2E3440)
val TextSecondary    = Color(0xFF6A6762)
val DarkBg           = Color(0xFF1A1C20)
val DarkCard         = Color(0xFF252830)
val SemanticSuccess  = Color(0xFF39AD3E)
val SemanticError    = Color(0xFFDE271A)
val CalmPurple       = Color(0xFF8E82C8)

// Алиасы — старые имена ссылаются на новые цвета
val DecisionBlue   = CalmBlue
val DecisionTeal   = SageGreen
val DecisionGold   = WarmAmber
val DecisionGreen  = SemanticSuccess
val DecisionRed    = SemanticError
val DecisionPurple = CalmPurple

private val LightColorScheme = lightColorScheme(
    primary              = CalmBlue,
    onPrimary            = Color.White,
    primaryContainer     = CalmBlueSurface,
    onPrimaryContainer   = CalmBlueDark,
    secondary            = SageGreen,
    onSecondary          = Color.White,
    secondaryContainer   = SageGreenSurface,
    onSecondaryContainer = SageGreenDark,
    tertiary             = WarmAmber,
    onTertiary           = Color.White,
    tertiaryContainer    = WarmAmberSurface,
    onTertiaryContainer  = Color(0xFF7A5A10),
    background           = WarmBg,
    onBackground         = TextPrimary,
    surface              = WarmCard,
    onSurface            = TextPrimary,
    surfaceVariant       = Color(0xFFF0EDE7),
    onSurfaceVariant     = TextSecondary,
    error                = SemanticError,
    onError              = Color.White,
    outline              = WarmBorder,
    outlineVariant       = Color(0xFFD8D5CF)
)

private val DarkColorScheme = darkColorScheme(
    primary              = CalmBlueLight,
    onPrimary            = Color(0xFF1A2A3A),
    primaryContainer     = Color(0xFF1E2C3C),
    onPrimaryContainer   = CalmBlueLight,
    secondary            = SageGreenLight,
    onSecondary          = Color(0xFF1A2C28),
    secondaryContainer   = Color(0xFF1A2C28),
    onSecondaryContainer = SageGreenLight,
    tertiary             = Color(0xFFE8C88A),
    onTertiary           = Color(0xFF2A1E00),
    tertiaryContainer    = Color(0xFF2A2010),
    onTertiaryContainer  = Color(0xFFE8C88A),
    background           = DarkBg,
    onBackground         = Color(0xFFE8E5E0),
    surface              = DarkCard,
    onSurface            = Color(0xFFE8E5E0),
    surfaceVariant       = Color(0xFF1E2028),
    onSurfaceVariant     = Color(0xFFA8A5A0),
    error                = Color(0xFFCF9090),
    onError              = Color(0xFF3A1010),
    outline              = Color(0x14FFFFFF),
    outlineVariant       = Color(0x20FFFFFF)
)

private val CalmTypography = Typography(
    headlineLarge  = TextStyle(fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 30.sp, letterSpacing = (-0.2).sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 26.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall      = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.05.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.06.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 0.08.sp)
)

@Composable
fun DecisionAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = CalmTypography,
        content     = content
    )
}