const fs = require('fs');

const themeCode = `package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val EBGaramond = FontFamily.Serif
val SourceSans3 = FontFamily.SansSerif

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = EBGaramond,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = EBGaramond,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 31.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SourceSans3,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SourceSans3,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SourceSans3,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.02.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SourceSans3,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E434C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF455A64),
    onPrimaryContainer = Color(0xFFBAD0DC),
    secondary = Color(0xFF4A626D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDE6F4),
    onSecondaryContainer = Color(0xFF506873),
    tertiary = Color(0xFF384144),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF4F585C),
    onTertiaryContainer = Color(0xFFC5CED2),
    background = Color(0xFFF8FAFB),
    onBackground = Color(0xFF191C1D),
    surface = Color(0xFFF8FAFB),
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFE1E3E4),
    onSurfaceVariant = Color(0xFF42474A),
    outline = Color(0xFF73787B),
    outlineVariant = Color(0xFFC2C7CA),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF93000A)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB4CAD6),
    onPrimary = Color(0xFF00344A),
    primaryContainer = Color(0xFF2E434C),
    onPrimaryContainer = Color(0xFFBAD0DC),
    secondary = Color(0xFFB1CAD7),
    onSecondary = Color(0xFF1B333E),
    secondaryContainer = Color(0xFF334A55),
    onSecondaryContainer = Color(0xFFCDE6F4),
    tertiary = Color(0xFFBFC8CC),
    onTertiary = Color(0xFF2A3235),
    tertiaryContainer = Color(0xFF3F484C),
    onTertiaryContainer = Color(0xFFC5CED2),
    background = Color(0xFF191C1D),
    onBackground = Color(0xFFE1E3E4),
    surface = Color(0xFF191C1D),
    onSurface = Color(0xFFE1E3E4),
    surfaceVariant = Color(0xFF42474A),
    onSurfaceVariant = Color(0xFFC2C7CA),
    outline = Color(0xFF8C9194),
    outlineVariant = Color(0xFF42474A),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun LedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
`;
fs.writeFileSync('app/src/main/java/com/example/ui/theme/Theme.kt', themeCode);
