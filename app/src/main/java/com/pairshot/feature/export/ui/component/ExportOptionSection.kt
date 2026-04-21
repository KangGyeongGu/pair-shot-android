package com.pairshot.feature.export.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.ui.component.SettingsCard
import com.pairshot.core.ui.component.SettingsDivider
import com.pairshot.core.ui.component.SettingsSwitchItem
import com.pairshot.feature.export.ui.viewmodel.ExportFormat

@Composable
internal fun ExportIncludeSection(
    beforeCount: Int,
    afterCount: Int,
    combinedCount: Int,
    includeBefore: Boolean,
    includeAfter: Boolean,
    includeCombined: Boolean,
    onIncludeBeforeChange: (Boolean) -> Unit,
    onIncludeAfterChange: (Boolean) -> Unit,
    onIncludeCombinedChange: (Boolean) -> Unit,
) {
    SettingsCard {
        SettingsSwitchItem(
            label = "Before 원본",
            checked = includeBefore,
            onCheckedChange = onIncludeBeforeChange,
            enabled = beforeCount > 0,
        )
        SettingsDivider()
        SettingsSwitchItem(
            label = "After 원본",
            checked = includeAfter,
            onCheckedChange = onIncludeAfterChange,
            enabled = afterCount > 0,
        )
        SettingsDivider()
        SettingsSwitchItem(
            label = "합성 이미지",
            checked = includeCombined,
            onCheckedChange = onIncludeCombinedChange,
            enabled = combinedCount > 0,
        )
    }
}

@Composable
internal fun ExportFormatSection(
    exportFormat: ExportFormat,
    onExportFormatChange: (ExportFormat) -> Unit,
) {
    SettingsCard {
        ExportFormatItem(
            label = "이미지 개별",
            selected = exportFormat == ExportFormat.INDIVIDUAL,
            onClick = { onExportFormatChange(ExportFormat.INDIVIDUAL) },
        )
        SettingsDivider()
        ExportFormatItem(
            label = "ZIP 압축",
            selected = exportFormat == ExportFormat.ZIP,
            onClick = { onExportFormatChange(ExportFormat.ZIP) },
        )
    }
}

@Composable
internal fun ExportWatermarkSection(
    applyWatermark: Boolean,
    onApplyWatermarkChange: (Boolean) -> Unit,
    onWatermarkSettingsClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    SettingsCard {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(PairShotSpacing.inputRow)
                    .padding(start = PairShotSpacing.cardPadding, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "워터마크 적용",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = applyWatermark,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onApplyWatermarkChange(it)
                },
                colors =
                    SwitchDefaults.colors(
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                modifier = Modifier.scale(0.67f),
            )
            VerticalDivider(
                modifier =
                    Modifier
                        .height(20.dp)
                        .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            IconButton(onClick = onWatermarkSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "워터마크 설정",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun ExportCombineSection(
    applyCombineConfig: Boolean,
    onApplyCombineConfigChange: (Boolean) -> Unit,
    onCombineSettingsClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    SettingsCard {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(PairShotSpacing.inputRow)
                    .padding(start = PairShotSpacing.cardPadding, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "합성 옵션 적용",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = applyCombineConfig,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onApplyCombineConfigChange(it)
                },
                colors =
                    SwitchDefaults.colors(
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                modifier = Modifier.scale(0.67f),
            )
            VerticalDivider(
                modifier =
                    Modifier
                        .height(20.dp)
                        .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            IconButton(onClick = onCombineSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "합성 설정",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ExportFormatItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .height(PairShotSpacing.inputRow)
                .padding(horizontal = PairShotSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        RadioButton(selected = selected, onClick = onClick)
    }
}
