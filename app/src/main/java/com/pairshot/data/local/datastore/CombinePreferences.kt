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
import com.pairshot.core.domain.combine.CombineConfig
import com.pairshot.core.domain.combine.CombineLayout
import com.pairshot.core.domain.combine.LabelAnchor
import com.pairshot.core.domain.combine.LabelPosition
import com.pairshot.core.domain.combine.LabelPositionMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.combineDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "combine_settings",
)

@Singleton
class CombinePreferences
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private object Keys {
            val LAYOUT = stringPreferencesKey("combine_layout")
            val BORDER_ENABLED = booleanPreferencesKey("combine_border_enabled")
            val BORDER_THICKNESS = intPreferencesKey("combine_border_thickness_dp")
            val BORDER_COLOR_ARGB = intPreferencesKey("combine_border_color_argb")
            val LABEL_ENABLED = booleanPreferencesKey("combine_label_enabled")
            val BEFORE_LABEL = stringPreferencesKey("combine_before_label")
            val AFTER_LABEL = stringPreferencesKey("combine_after_label")
            val LABEL_POSITION = stringPreferencesKey("combine_label_position")
            val LABEL_SIZE_RATIO = floatPreferencesKey("combine_label_size_ratio")
            val LABEL_TEXT_COLOR_ARGB = intPreferencesKey("combine_label_text_color_argb")
            val LABEL_BG_COLOR_ARGB = intPreferencesKey("combine_label_bg_color_argb")
            val LABEL_BG_ALPHA = floatPreferencesKey("combine_label_bg_alpha")
            val LABEL_BG_ENABLED = booleanPreferencesKey("label_bg_enabled")
            val LABEL_BG_MATCHES_BORDER = booleanPreferencesKey("label_bg_matches_border")
            val LABEL_POSITION_MODE = stringPreferencesKey("label_position_mode")
            val BEFORE_LABEL_ANCHOR = stringPreferencesKey("before_label_anchor")
            val AFTER_LABEL_ANCHOR = stringPreferencesKey("after_label_anchor")
            val LABEL_BG_CORNER = intPreferencesKey("label_bg_corner_dp")
        }

        val configFlow: Flow<CombineConfig> =
            context.combineDataStore.data.map { prefs ->
                CombineConfig(
                    layout =
                        prefs[Keys.LAYOUT]?.let { name ->
                            runCatching { CombineLayout.valueOf(name) }.getOrNull()
                        } ?: CombineLayout.HORIZONTAL,
                    borderEnabled = prefs[Keys.BORDER_ENABLED] ?: false,
                    borderThicknessDp = prefs[Keys.BORDER_THICKNESS] ?: 12,
                    borderColorArgb = prefs[Keys.BORDER_COLOR_ARGB] ?: 0xFFFFFFFF.toInt(),
                    labelEnabled = prefs[Keys.LABEL_ENABLED] ?: true,
                    beforeLabel = prefs[Keys.BEFORE_LABEL] ?: "BEFORE",
                    afterLabel = prefs[Keys.AFTER_LABEL] ?: "AFTER",
                    labelPosition =
                        prefs[Keys.LABEL_POSITION]?.let { name ->
                            runCatching { LabelPosition.valueOf(name) }.getOrNull()
                        } ?: LabelPosition.BOTTOM,
                    labelSizeRatio = prefs[Keys.LABEL_SIZE_RATIO] ?: 0.04f,
                    labelTextColorArgb = prefs[Keys.LABEL_TEXT_COLOR_ARGB] ?: 0xFFFFFFFF.toInt(),
                    labelBgColorArgb = prefs[Keys.LABEL_BG_COLOR_ARGB] ?: 0xFF000000.toInt(),
                    labelBgAlpha = prefs[Keys.LABEL_BG_ALPHA] ?: 0.45f,
                    labelBgEnabled = prefs[Keys.LABEL_BG_ENABLED] ?: true,
                    labelBgMatchesBorder = prefs[Keys.LABEL_BG_MATCHES_BORDER] ?: true,
                    labelPositionMode =
                        prefs[Keys.LABEL_POSITION_MODE]?.let { name ->
                            runCatching { LabelPositionMode.valueOf(name) }.getOrNull()
                        } ?: LabelPositionMode.FULL_WIDTH,
                    beforeLabelAnchor =
                        prefs[Keys.BEFORE_LABEL_ANCHOR]?.let { name ->
                            runCatching { LabelAnchor.valueOf(name) }.getOrNull()
                        } ?: LabelAnchor.BOTTOM_LEFT,
                    afterLabelAnchor =
                        prefs[Keys.AFTER_LABEL_ANCHOR]?.let { name ->
                            runCatching { LabelAnchor.valueOf(name) }.getOrNull()
                        } ?: LabelAnchor.BOTTOM_LEFT,
                    labelBgCornerDp = prefs[Keys.LABEL_BG_CORNER] ?: 0,
                )
            }

        suspend fun saveConfig(config: CombineConfig) {
            context.combineDataStore.edit { prefs ->
                prefs[Keys.LAYOUT] = config.layout.name
                prefs[Keys.BORDER_ENABLED] = config.borderEnabled
                prefs[Keys.BORDER_THICKNESS] = config.borderThicknessDp
                prefs[Keys.BORDER_COLOR_ARGB] = config.borderColorArgb
                prefs[Keys.LABEL_ENABLED] = config.labelEnabled
                prefs[Keys.BEFORE_LABEL] = config.beforeLabel
                prefs[Keys.AFTER_LABEL] = config.afterLabel
                prefs[Keys.LABEL_POSITION] = config.labelPosition.name
                prefs[Keys.LABEL_SIZE_RATIO] = config.labelSizeRatio
                prefs[Keys.LABEL_TEXT_COLOR_ARGB] = config.labelTextColorArgb
                prefs[Keys.LABEL_BG_COLOR_ARGB] = config.labelBgColorArgb
                prefs[Keys.LABEL_BG_ALPHA] = config.labelBgAlpha
                prefs[Keys.LABEL_BG_ENABLED] = config.labelBgEnabled
                prefs[Keys.LABEL_BG_MATCHES_BORDER] = config.labelBgMatchesBorder
                prefs[Keys.LABEL_POSITION_MODE] = config.labelPositionMode.name
                prefs[Keys.BEFORE_LABEL_ANCHOR] = config.beforeLabelAnchor.name
                prefs[Keys.AFTER_LABEL_ANCHOR] = config.afterLabelAnchor.name
                prefs[Keys.LABEL_BG_CORNER] = config.labelBgCornerDp
            }
        }
    }
