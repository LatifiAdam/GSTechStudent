package com.gstech.student.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = GSBluePrimary,
    onPrimary = GSSurfaceLight,
    secondary = GSTeal,
    onSecondary = GSSurfaceLight,
    background = GSBackgroundLight,
    onBackground = GSTextPrimaryLight,
    surface = GSSurfaceLight,
    onSurface = GSTextPrimaryLight,
    error = GSDanger,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF168BE0),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF13C5BE),
    onSecondary = Color(0xFF07131E),
    background = Color(0xFF0D1722),
    onBackground = Color(0xFFF2F5F8),
    surface = Color(0xFF1B2D3F),
    onSurface = Color(0xFFF2F5F8),
    surfaceVariant = Color(0xFF22384C),
    onSurfaceVariant = Color(0xFFB5C1CC),
    outline = Color(0xFF38546B),
    outlineVariant = Color(0xFF2C4357),
    error = GSDanger,
)

val GSTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
)

@Composable
fun GSTechStudentTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = GSTypography,
        content = content,
    )
}
