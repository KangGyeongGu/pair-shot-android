package com.pairshot.core.data.repository

import com.pairshot.core.datastore.AppPreferences
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.model.AppSettings
import com.pairshot.core.model.ExportFormat
import com.pairshot.core.model.ExportPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsRepositoryImpl
    @Inject
    constructor(
        private val appPreferences: AppPreferences,
    ) : AppSettingsRepository {
        private val baseSettingsFlow: Flow<AppSettings> =
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

        private val cameraSettingsFlow: Flow<AppSettings> =
            combine(
                appPreferences.cameraGridEnabled,
                appPreferences.cameraLevelEnabled,
                appPreferences.cameraFlashMode,
                appPreferences.cameraNightMode,
                appPreferences.cameraHdr,
            ) { grid, level, flash, night, hdr ->
                AppSettings(
                    cameraGridEnabled = grid,
                    cameraLevelEnabled = level,
                    cameraFlashMode = flash,
                    cameraNightModeEnabled = night,
                    cameraHdrEnabled = hdr,
                )
            }

        override val settingsFlow: Flow<AppSettings> =
            combine(baseSettingsFlow, cameraSettingsFlow) { base, camera ->
                base.copy(
                    cameraGridEnabled = camera.cameraGridEnabled,
                    cameraLevelEnabled = camera.cameraLevelEnabled,
                    cameraFlashMode = camera.cameraFlashMode,
                    cameraNightModeEnabled = camera.cameraNightModeEnabled,
                    cameraHdrEnabled = camera.cameraHdrEnabled,
                )
            }

        override suspend fun getCurrent(): AppSettings = settingsFlow.first()

        override suspend fun updateJpegQuality(quality: Int) = appPreferences.setJpegQuality(quality)

        override suspend fun updateFileNamePrefix(prefix: String) = appPreferences.setFileNamePrefix(prefix)

        override suspend fun updateOverlayEnabled(enabled: Boolean) = appPreferences.setOverlayEnabled(enabled)

        override suspend fun updateOverlayAlpha(alpha: Float) = appPreferences.setOverlayAlpha(alpha)

        override suspend fun updateCameraGridEnabled(enabled: Boolean) = appPreferences.setCameraGridEnabled(enabled)

        override suspend fun updateCameraLevelEnabled(enabled: Boolean) = appPreferences.setCameraLevelEnabled(enabled)

        override suspend fun updateCameraFlashMode(mode: String) = appPreferences.setCameraFlashMode(mode)

        override suspend fun updateCameraNightMode(enabled: Boolean) = appPreferences.setCameraNightMode(enabled)

        override suspend fun updateCameraHdr(enabled: Boolean) = appPreferences.setCameraHdr(enabled)

        override suspend fun getLastExportPreset(): ExportPreset =
            combine(
                appPreferences.exportFormat,
                appPreferences.exportIncludeBefore,
                appPreferences.exportIncludeAfter,
                appPreferences.exportIncludeCombined,
            ) { format, before, after, combined ->
                ExportPreset(
                    format = runCatching { ExportFormat.valueOf(format) }.getOrDefault(ExportFormat.ZIP),
                    includeBefore = before,
                    includeAfter = after,
                    includeCombined = combined,
                )
            }.first()

        override suspend fun saveLastExportPreset(preset: ExportPreset) {
            appPreferences.saveExportPreset(
                format = preset.format.name,
                includeBefore = preset.includeBefore,
                includeAfter = preset.includeAfter,
                includeCombined = preset.includeCombined,
            )
        }
    }
