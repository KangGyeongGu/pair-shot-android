package com.pairshot.data.repository.settings

import com.pairshot.core.model.AppSettings
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.model.ExportPreset
import com.pairshot.core.datastore.AppPreferences
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

        override suspend fun updateJpegQuality(quality: Int) = appPreferences.setJpegQuality(quality)

        override suspend fun updateFileNamePrefix(prefix: String) = appPreferences.setFileNamePrefix(prefix)

        override suspend fun updateOverlayEnabled(enabled: Boolean) = appPreferences.setOverlayEnabled(enabled)

        override suspend fun updateOverlayAlpha(alpha: Float) = appPreferences.setOverlayAlpha(alpha)

        override suspend fun updateCameraGridEnabled(enabled: Boolean) = appPreferences.setCameraGridEnabled(enabled)

        override suspend fun updateCameraLevelEnabled(enabled: Boolean) = appPreferences.setCameraLevelEnabled(enabled)

        override suspend fun updateCameraFlashMode(mode: String) = appPreferences.setCameraFlashMode(mode)

        override suspend fun updateCameraNightMode(enabled: Boolean) = appPreferences.setCameraNightMode(enabled)

        override suspend fun updateCameraHdr(enabled: Boolean) = appPreferences.setCameraHdr(enabled)

        override suspend fun getLastExportPreset(): ExportPreset {
            val format = appPreferences.exportFormat.first()
            val before = appPreferences.exportIncludeBefore.first()
            val after = appPreferences.exportIncludeAfter.first()
            val combined = appPreferences.exportIncludeCombined.first()
            return ExportPreset(format = format, includeBefore = before, includeAfter = after, includeCombined = combined)
        }

        override suspend fun saveLastExportPreset(preset: ExportPreset) {
            appPreferences.setExportFormat(preset.format)
            appPreferences.setExportIncludeBefore(preset.includeBefore)
            appPreferences.setExportIncludeAfter(preset.includeAfter)
            appPreferences.setExportIncludeCombined(preset.includeCombined)
        }
    }
