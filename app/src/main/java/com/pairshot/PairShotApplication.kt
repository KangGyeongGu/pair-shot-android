package com.pairshot

import android.app.Application
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.feature.settings.theme.AppTheme
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PairShotApplication : Application() {
    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            val name = appSettingsRepository.appThemeNameFlow.first()
            AppTheme.fromName(name).apply()
        }
    }
}
