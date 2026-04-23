package com.pairshot.feature.settings.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pairshot.core.model.LogoPosition
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.ui.component.SettingsCard
import com.pairshot.core.ui.component.SettingsDivider
import com.pairshot.core.ui.component.SettingsItem
import com.pairshot.core.ui.component.SettingsSliderItem
import com.pairshot.feature.settings.R
import java.io.File
import kotlin.math.roundToInt

private val logoPositionOrder =
    listOf(
        LogoPosition.TOP_LEFT,
        LogoPosition.TOP_CENTER,
        LogoPosition.TOP_RIGHT,
        LogoPosition.CENTER_LEFT,
        LogoPosition.CENTER,
        LogoPosition.CENTER_RIGHT,
        LogoPosition.BOTTOM_LEFT,
        LogoPosition.BOTTOM_CENTER,
        LogoPosition.BOTTOM_RIGHT,
    )

@Composable
internal fun WatermarkLogoSection(
    watermarkConfig: WatermarkConfig,
    onWatermarkConfigChange: (WatermarkConfig) -> Unit,
    onSelectLogo: () -> Unit,
) {
    SettingsCard {
        SettingsItem(
            label = stringResource(R.string.watermark_logo_label),
            trailing =
                if (watermarkConfig.logoPath.isNotEmpty()) {
                    File(watermarkConfig.logoPath).name
                } else {
                    stringResource(R.string.watermark_logo_choose)
                },
            onClick = onSelectLogo,
        )
        SettingsDivider()
        PositionPicker3x3Row(
            label = stringResource(R.string.watermark_field_position),
            positions = logoPositionOrder,
            selectedPosition = watermarkConfig.logoPosition,
            onPositionChange = { position ->
                onWatermarkConfigChange(watermarkConfig.copy(logoPosition = position))
            },
        )
        SettingsDivider()
        SettingsSliderItem(
            label = stringResource(R.string.watermark_field_size),
            value = watermarkConfig.logoSizeRatio,
            valueRange = 0f..1.0f,
            valueLabel = { "${(it * 100).roundToInt()}%" },
            onValueChange = { v -> onWatermarkConfigChange(watermarkConfig.copy(logoSizeRatio = v)) },
            onLiveUpdate = { v -> onWatermarkConfigChange(watermarkConfig.copy(logoSizeRatio = v)) },
        )
        SettingsDivider()
        SettingsSliderItem(
            label = stringResource(R.string.watermark_field_opacity),
            value = watermarkConfig.logoAlpha,
            valueRange = 0f..1f,
            valueLabel = { "${(it * 100).roundToInt()}%" },
            onValueChange = { v -> onWatermarkConfigChange(watermarkConfig.copy(logoAlpha = v)) },
            onLiveUpdate = { v -> onWatermarkConfigChange(watermarkConfig.copy(logoAlpha = v)) },
        )
    }
}
