package com.gstech.student.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeController {
    private const val PREFS = "gstech_preferences"
    private const val KEY_DARK = "dark_mode"
    var darkTheme by mutableStateOf(false)
        private set

    fun initialize(context: Context) {
        darkTheme = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_DARK, false)
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        darkTheme = enabled
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_DARK, enabled).apply()
    }
}
