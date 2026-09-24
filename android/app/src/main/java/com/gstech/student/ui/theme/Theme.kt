package com.gstech.student.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = GSBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEEFF),
    onPrimaryContainer = GSBlueDark,
    secondary = GSTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9F4F2),
    onSecondaryContainer = Color(0xFF0B5E5A),
    background = GSBackgroundLight,
    onBackground = GSTextPrimaryLight,
    surface = GSSurfaceLight,
    onSurface = GSTextPrimaryLight,
    surfaceVariant = GSSurfaceTint,
    onSurfaceVariant = GSTextSecondaryLight,
    outline = Color(0xFFCEDBE7),
    outlineVariant = GSDividerLight,
    error = GSDanger,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF55AEE3),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF064E74),
    onPrimaryContainer = Color(0xFFD8F0FF),
    secondary = Color(0xFF55D2CC),
    onSecondary = Color(0xFF003735),
    secondaryContainer = Color(0xFF075C59),
    onSecondaryContainer = Color(0xFFB8EFEC),
    background = Color(0xFF0D1722),
    onBackground = Color(0xFFF4F7FA),
    surface = Color(0xFF142331),
    onSurface = Color(0xFFF4F7FA),
    surfaceVariant = Color(0xFF1D3040),
    onSurfaceVariant = Color(0xFFB4C4D1),
    outline = Color(0xFF395569),
    outlineVariant = Color(0xFF2B4355),
    error = GSDanger,
)

val GSTypography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 23.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 17.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 17.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp),
)

val GSShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
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
        shapes = GSShapes,
        content = content,
    )
}
