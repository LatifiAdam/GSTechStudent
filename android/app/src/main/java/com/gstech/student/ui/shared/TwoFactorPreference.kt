package com.gstech.student.ui.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object TwoFactorPreference {
    var enabled by mutableStateOf(false)
        private set
    fun initialize(context: android.content.Context) { enabled = context.getSharedPreferences("gstech_preferences", 0).getBoolean("two_factor", false) }
    fun setEnabled(context: android.content.Context, value: Boolean) { enabled = value; context.getSharedPreferences("gstech_preferences", 0).edit().putBoolean("two_factor", value).apply() }
}
