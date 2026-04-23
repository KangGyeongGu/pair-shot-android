package com.pairshot

import android.app.Application
import com.pairshot.feature.settings.theme.applyAppThemeFromPreference
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PairShotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applyAppThemeFromPreference()
    }
}
