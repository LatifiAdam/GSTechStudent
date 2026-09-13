package com.gstech.student.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// GSTech palette. Light-mode base colors are kept here; the composable accessors
// below follow MaterialTheme so every screen actually changes with dark mode.
val GSBluePrimary = Color(0xFF0F6DB7)
val GSBlueDark = Color(0xFF0B3D63)
val GSTeal = Color(0xFF10B8B0)
val GSBackgroundLight = Color(0xFFF2F6FA)
// Light-blue surface used by cards/frames throughout the app.
val GSSurfaceLight = Color(0xFFEAF5FF)
val GSTextPrimaryLight = Color(0xFF102A43)
val GSTextSecondaryLight = Color(0xFF6B7A90)
val GSDividerLight = Color(0xFFE3E9F0)
val GSSuccess = Color(0xFF2ECC71)
val GSDanger = Color(0xFFE94B3C)
val GSWarning = Color(0xFFF2A93B)

val GSBackground: Color @Composable get() = MaterialTheme.colorScheme.background
val GSSurface: Color @Composable get() = MaterialTheme.colorScheme.surface
val GSTextPrimary: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val GSTextSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val GSDivider: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
