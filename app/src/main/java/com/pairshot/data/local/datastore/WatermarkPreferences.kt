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
import com.pairshot.core.domain.model.LogoPosition
import com.pairshot.core.domain.model.WatermarkConfig
import com.pairshot.core.domain.model.WatermarkType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.watermarkDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "watermark_settings",
)

@Singleton
class WatermarkPreferences
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private object Keys {
            val ENABLED = booleanPreferencesKey("enabled")
            val TYPE = stringPreferencesKey("type")
            val TEXT = stringPreferencesKey("text")
            val ALPHA = floatPreferencesKey("alpha")
            val DIAGONAL_COUNT = intPreferencesKey("diagonal_count")
            val REPEAT_DENSITY = floatPreferencesKey("repeat_density")
            val TEXT_SIZE_RATIO = floatPreferencesKey("text_size_ratio")
            val LOGO_PATH = stringPreferencesKey("logo_path")
            val LOGO_POSITION = stringPreferencesKey("logo_position")
            val LOGO_SIZE_RATIO = floatPreferencesKey("logo_size_ratio")
            val LOGO_ALPHA = floatPreferencesKey("logo_alpha")
        }

        val watermarkConfigFlow: Flow<WatermarkConfig> =
            context.watermarkDataStore.data.map { prefs ->
                WatermarkConfig(
                    enabled = prefs[Keys.ENABLED] ?: false,
                    type =
                        prefs[Keys.TYPE]?.let { name ->
                            runCatching { WatermarkType.valueOf(name) }.getOrNull()
                        } ?: WatermarkType.TEXT,
                    text = prefs[Keys.TEXT] ?: "",
                    alpha = prefs[Keys.ALPHA] ?: 0.3f,
                    diagonalCount = prefs[Keys.DIAGONAL_COUNT] ?: 5,
                    repeatDensity = prefs[Keys.REPEAT_DENSITY] ?: 1.0f,
                    textSizeRatio = prefs[Keys.TEXT_SIZE_RATIO] ?: 0.03f,
                    logoPath = prefs[Keys.LOGO_PATH] ?: "",
                    logoPosition =
                        prefs[Keys.LOGO_POSITION]?.let { name ->
                            runCatching { LogoPosition.valueOf(name) }.getOrNull()
                        } ?: LogoPosition.BOTTOM_RIGHT,
                    logoSizeRatio = prefs[Keys.LOGO_SIZE_RATIO] ?: 0.15f,
                    logoAlpha = prefs[Keys.LOGO_ALPHA] ?: 0.5f,
                )
            }

        suspend fun saveConfig(config: WatermarkConfig) {
            context.watermarkDataStore.edit { prefs ->
                prefs[Keys.ENABLED] = config.enabled
                prefs[Keys.TYPE] = config.type.name
                prefs[Keys.TEXT] = config.text
                prefs[Keys.ALPHA] = config.alpha
                prefs[Keys.DIAGONAL_COUNT] = config.diagonalCount
                prefs[Keys.REPEAT_DENSITY] = config.repeatDensity
                prefs[Keys.TEXT_SIZE_RATIO] = config.textSizeRatio
                prefs[Keys.LOGO_PATH] = config.logoPath
                prefs[Keys.LOGO_POSITION] = config.logoPosition.name
                prefs[Keys.LOGO_SIZE_RATIO] = config.logoSizeRatio
                prefs[Keys.LOGO_ALPHA] = config.logoAlpha
            }
        }
    }
