package com.pairshot.feature.settings.data.repository

import com.pairshot.data.local.datastore.AppPreferences
import com.pairshot.feature.settings.domain.model.AppSettings
import com.pairshot.feature.settings.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsRepositoryImpl
    @Inject
    constructor(
        private val appPreferences: AppPreferences,
    ) : AppSettingsRepository {
        override val settingsFlow: Flow<AppSettings> =
            combine(
                appPreferences.jpegQuality,
                appPreferences.fileNamePrefix,
                appPreferences.overlayEnabled,
                appPreferences.overlayAlpha,
            ) { quality, prefix, enabled, alpha ->
                AppSettings(
                    jpegQuality = quality,
                    fileNamePrefix = prefix,
                    overlayEnabled = enabled,
                    defaultOverlayAlpha = alpha,
                )
            }

        override suspend fun updateJpegQuality(quality: Int) = appPreferences.setJpegQuality(quality)

        override suspend fun updateFileNamePrefix(prefix: String) = appPreferences.setFileNamePrefix(prefix)

        override suspend fun updateOverlayEnabled(enabled: Boolean) = appPreferences.setOverlayEnabled(enabled)

        override suspend fun updateOverlayAlpha(alpha: Float) = appPreferences.setOverlayAlpha(alpha)
    }
