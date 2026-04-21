package com.pairshot.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings",
)

@Singleton
class AppPreferences
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private object Keys {
            val JPEG_QUALITY = intPreferencesKey("jpeg_quality")
            val FILE_NAME_PREFIX = stringPreferencesKey("file_name_prefix")
            val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
            val OVERLAY_ALPHA = floatPreferencesKey("overlay_alpha")
            val CAMERA_GRID_ENABLED = booleanPreferencesKey("camera_grid_enabled")
            val CAMERA_LEVEL_ENABLED = booleanPreferencesKey("camera_level_enabled")
            val CAMERA_FLASH_MODE = stringPreferencesKey("camera_flash_mode")
            val CAMERA_NIGHT_MODE = booleanPreferencesKey("camera_night_mode")
            val CAMERA_HDR = booleanPreferencesKey("camera_hdr")
            val EXPORT_FORMAT = stringPreferencesKey("export_format")
            val EXPORT_INCLUDE_BEFORE = booleanPreferencesKey("export_include_before")
            val EXPORT_INCLUDE_AFTER = booleanPreferencesKey("export_include_after")
            val EXPORT_INCLUDE_COMBINED = booleanPreferencesKey("export_include_combined")
        }

        val jpegQuality: Flow<Int> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.JPEG_QUALITY] ?: 85
            }

        val fileNamePrefix: Flow<String> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.FILE_NAME_PREFIX] ?: "PAIRSHOT"
            }

        val overlayEnabled: Flow<Boolean> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.OVERLAY_ENABLED] ?: true
            }

        val overlayAlpha: Flow<Float> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.OVERLAY_ALPHA] ?: 0.5f
            }

        suspend fun setJpegQuality(quality: Int) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.JPEG_QUALITY] = quality
            }
        }

        suspend fun setFileNamePrefix(prefix: String) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.FILE_NAME_PREFIX] = prefix
            }
        }

        suspend fun setOverlayEnabled(enabled: Boolean) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.OVERLAY_ENABLED] = enabled
            }
        }

        suspend fun setOverlayAlpha(alpha: Float) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.OVERLAY_ALPHA] = alpha
            }
        }

        val cameraGridEnabled: Flow<Boolean> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.CAMERA_GRID_ENABLED] ?: false
            }

        val cameraLevelEnabled: Flow<Boolean> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.CAMERA_LEVEL_ENABLED] ?: false
            }

        val cameraFlashMode: Flow<String> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.CAMERA_FLASH_MODE] ?: "OFF"
            }

        val cameraNightMode: Flow<Boolean> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.CAMERA_NIGHT_MODE] ?: false
            }

        val cameraHdr: Flow<Boolean> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.CAMERA_HDR] ?: false
            }

        suspend fun setCameraGridEnabled(enabled: Boolean) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.CAMERA_GRID_ENABLED] = enabled
            }
        }

        suspend fun setCameraLevelEnabled(enabled: Boolean) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.CAMERA_LEVEL_ENABLED] = enabled
            }
        }

        suspend fun setCameraFlashMode(mode: String) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.CAMERA_FLASH_MODE] = mode
            }
        }

        suspend fun setCameraNightMode(enabled: Boolean) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.CAMERA_NIGHT_MODE] = enabled
            }
        }

        suspend fun setCameraHdr(enabled: Boolean) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.CAMERA_HDR] = enabled
            }
        }

        val exportFormat: Flow<String> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.EXPORT_FORMAT] ?: "ZIP"
            }

        val exportIncludeBefore: Flow<Boolean> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.EXPORT_INCLUDE_BEFORE] ?: true
            }

        val exportIncludeAfter: Flow<Boolean> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.EXPORT_INCLUDE_AFTER] ?: true
            }

        val exportIncludeCombined: Flow<Boolean> =
            context.appDataStore.data.map { prefs ->
                prefs[Keys.EXPORT_INCLUDE_COMBINED] ?: true
            }

        suspend fun setExportFormat(format: String) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.EXPORT_FORMAT] = format
            }
        }

        suspend fun setExportIncludeBefore(value: Boolean) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.EXPORT_INCLUDE_BEFORE] = value
            }
        }

        suspend fun setExportIncludeAfter(value: Boolean) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.EXPORT_INCLUDE_AFTER] = value
            }
        }

        suspend fun setExportIncludeCombined(value: Boolean) {
            context.appDataStore.edit { prefs ->
                prefs[Keys.EXPORT_INCLUDE_COMBINED] = value
            }
        }
    }
