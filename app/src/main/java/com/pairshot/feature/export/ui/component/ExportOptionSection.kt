package com.pairshot.feature.export.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pairshot.core.designsystem.PairShotSpacing
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
    Text(
        text = "포함할 항목",
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.size(PairShotSpacing.itemGap))
    Column {
        ExportCheckboxRow(
            label = "Before 원본 (${beforeCount}장)",
            checked = includeBefore,
            onCheckedChange = onIncludeBeforeChange,
            enabled = beforeCount > 0,
        )
        ExportCheckboxRow(
            label = "After 원본 (${afterCount}장)",
            checked = includeAfter,
            onCheckedChange = onIncludeAfterChange,
            enabled = afterCount > 0,
        )
        ExportCheckboxRow(
            label = "합성 이미지 (${combinedCount}장)",
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
    Text(
        text = "내보내기 형식",
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.size(PairShotSpacing.itemGap))
    Column {
        ExportRadioButtonRow(
            label = "이미지 개별",
            selected = exportFormat == ExportFormat.INDIVIDUAL,
            onClick = { onExportFormatChange(ExportFormat.INDIVIDUAL) },
        )
        ExportRadioButtonRow(
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "워터마크",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onWatermarkSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "워터마크 설정",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(PairShotSpacing.iconSize),
            )
        }
    }
    ExportCheckboxRow(
        label = "워터마크 적용",
        checked = applyWatermark,
        onCheckedChange = onApplyWatermarkChange,
    )
}

@Composable
internal fun ExportCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = PairShotSpacing.cardPadding / 2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
        Spacer(modifier = Modifier.width(PairShotSpacing.iconTextGap))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}

@Composable
private fun ExportRadioButtonRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = PairShotSpacing.cardPadding / 2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(PairShotSpacing.iconTextGap))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
