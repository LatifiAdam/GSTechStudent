package com.gstech.student.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// GSTech design palette inspired by the supplied UI reference screens:
// calm blue accents, very light blue-gray canvas, crisp white surfaces.
val GSBluePrimary = Color(0xFF0B67A3)
val GSBlueDark = Color(0xFF0B3D63)
val GSTeal = Color(0xFF159C97)
val GSBackgroundLight = Color(0xFFF4F7FB)
val GSSurfaceLight = Color(0xFFFFFFFF)
val GSSurfaceTint = Color(0xFFE7F1FB)
val GSTextPrimaryLight = Color(0xFF172B3A)
val GSTextSecondaryLight = Color(0xFF6C7D8D)
val GSDividerLight = Color(0xFFDCE6EF)
val GSSuccess = Color(0xFF2FA36B)
val GSDanger = Color(0xFFD94B4B)
val GSWarning = Color(0xFFDB9A2B)

val GSBackground: Color @Composable get() = MaterialTheme.colorScheme.background
val GSSurface: Color @Composable get() = MaterialTheme.colorScheme.surface
val GSTextPrimary: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val GSTextSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val GSDivider: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
