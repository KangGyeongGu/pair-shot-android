package com.pairshot.core.domain.repository

import com.pairshot.core.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val settingsFlow: Flow<AppSettings>

    suspend fun updateJpegQuality(quality: Int)

    suspend fun updateFileNamePrefix(prefix: String)

    suspend fun updateOverlayEnabled(enabled: Boolean)

    suspend fun updateOverlayAlpha(alpha: Float)

    suspend fun updateCameraGridEnabled(enabled: Boolean)

    suspend fun updateCameraLevelEnabled(enabled: Boolean)

    suspend fun updateCameraFlashMode(mode: String)

    suspend fun updateCameraNightMode(enabled: Boolean)

    suspend fun updateCameraHdr(enabled: Boolean)
}
