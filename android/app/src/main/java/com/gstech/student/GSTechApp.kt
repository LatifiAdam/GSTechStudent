package com.gstech.student

import android.app.Application
import com.gstech.student.data.AppContainer
import com.gstech.student.ui.theme.ThemeController
import com.gstech.student.ui.shared.TwoFactorPreference

class GSTechApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        ThemeController.initialize(this)
        TwoFactorPreference.initialize(this)
        container = AppContainer(this)
    }
}
