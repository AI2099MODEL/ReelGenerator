package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Stitch Design System: The Archivist - Rose Quartz Archive
private val RoseQuartzColorScheme = lightColorScheme(
    primary = RoseQuartzPrimary,
    onPrimary = RoseQuartzOnPrimary,
    primaryContainer = RoseQuartzPrimaryContainer,
    onPrimaryContainer = RoseQuartzOnPrimaryContainer,
    secondary = RoseQuartzSecondary,
    onSecondary = RoseQuartzOnSecondary,
    secondaryContainer = RoseQuartzSecondaryContainer,
    onSecondaryContainer = RoseQuartzTextPrimary,
    tertiary = RoseQuartzSteel,
    onTertiary = RoseQuartzOnPrimary,
    tertiaryContainer = RoseQuartzSteelLight,
    onTertiaryContainer = RoseQuartzInkBlue,
    background = RoseQuartzBg,
    onBackground = RoseQuartzTextPrimary,
    surface = RoseQuartzSurface,
    onSurface = RoseQuartzTextPrimary,
    surfaceVariant = RoseQuartzContainer,
    onSurfaceVariant = RoseQuartzTextSecondary,
    surfaceContainerLowest = RoseQuartzContainerLowest,
    surfaceContainerLow = RoseQuartzContainerLow,
    surfaceContainer = RoseQuartzContainer,
    surfaceContainerHigh = RoseQuartzContainerHigh,
    surfaceContainerHighest = RoseQuartzContainerHighest,
    outline = RoseQuartzOutline,
    outlineVariant = RoseQuartzOutlineVariant,
    error = RoseQuartzError,
    onError = RoseQuartzOnError,
    errorContainer = RoseQuartzErrorContainer,
    onErrorContainer = RoseQuartzError
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.02).sp,
        color = RoseQuartzTextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = RoseQuartzTextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = RoseQuartzTextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        color = RoseQuartzTextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = RoseQuartzTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = RoseQuartzTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = RoseQuartzTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = RoseQuartzTextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = RoseQuartzTextSecondary
    )
)

@Composable
fun MyLyfeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RoseQuartzColorScheme,
        typography = AppTypography,
        content = content
    )
}

@Composable
fun LedgerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MyLyfeTheme(darkTheme = darkTheme, content = content)
}

