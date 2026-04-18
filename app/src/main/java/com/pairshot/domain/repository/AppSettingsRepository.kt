package com.pairshot.domain.repository

import com.pairshot.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val settingsFlow: Flow<AppSettings>

    suspend fun updateJpegQuality(quality: Int)

    suspend fun updateFileNamePrefix(prefix: String)

    suspend fun updateOverlayEnabled(enabled: Boolean)

    suspend fun updateOverlayAlpha(alpha: Float)
}
