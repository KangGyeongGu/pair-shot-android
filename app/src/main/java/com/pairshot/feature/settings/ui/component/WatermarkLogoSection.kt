package com.pairshot.feature.settings.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.model.LogoPosition
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.ui.component.SettingsCard
import com.pairshot.core.ui.component.SettingsDivider
import com.pairshot.core.ui.component.SettingsItem
import com.pairshot.core.ui.component.SettingsSliderItem
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
            label = "로고 등록",
            trailing =
                if (watermarkConfig.logoPath.isNotEmpty()) {
                    File(watermarkConfig.logoPath).name
                } else {
                    "선택"
                },
            onClick = onSelectLogo,
        )
        SettingsDivider()
        LogoPositionItem(
            selectedPosition = watermarkConfig.logoPosition,
            onPositionChange = { position ->
                onWatermarkConfigChange(watermarkConfig.copy(logoPosition = position))
            },
        )
        SettingsDivider()
        SettingsSliderItem(
            label = "로고 크기",
            value = watermarkConfig.logoSizeRatio,
            valueRange = 0f..1.0f,
            valueLabel = { "${(it * 100).roundToInt()}%" },
            onValueChange = { v -> onWatermarkConfigChange(watermarkConfig.copy(logoSizeRatio = v)) },
            onLiveUpdate = { v -> onWatermarkConfigChange(watermarkConfig.copy(logoSizeRatio = v)) },
        )
        SettingsDivider()
        SettingsSliderItem(
            label = "로고 투명도",
            value = watermarkConfig.logoAlpha,
            valueRange = 0f..1f,
            valueLabel = { "${(it * 100).roundToInt()}%" },
            onValueChange = { v -> onWatermarkConfigChange(watermarkConfig.copy(logoAlpha = v)) },
            onLiveUpdate = { v -> onWatermarkConfigChange(watermarkConfig.copy(logoAlpha = v)) },
        )
    }
}

@Composable
private fun LogoPositionItem(
    selectedPosition: LogoPosition,
    onPositionChange: (LogoPosition) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = PairShotSpacing.cardPadding,
                    vertical = PairShotSpacing.cardPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "로고 위치",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap),
        ) {
            logoPositionOrder.chunked(3).forEach { rowPositions ->
                Row(horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap)) {
                    rowPositions.forEach { position ->
                        val isSelected = position == selectedPosition
                        Box(
                            modifier =
                                Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(PairShotSpacing.iconSize)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                        },
                                    ).semantics { selected = isSelected }
                                    .clickable { onPositionChange(position) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
