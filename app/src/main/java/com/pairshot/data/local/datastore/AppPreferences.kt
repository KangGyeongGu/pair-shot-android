package com.pairshot.data.local.datastore

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
                prefs[Keys.OVERLAY_ALPHA] ?: 0.3f
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
    }
