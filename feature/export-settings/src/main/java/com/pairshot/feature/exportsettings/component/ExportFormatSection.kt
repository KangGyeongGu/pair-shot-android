package com.pairshot.feature.exportsettings.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.model.ExportFormat
import com.pairshot.core.ui.component.SettingsCard
import com.pairshot.core.ui.component.SettingsDivider
import com.pairshot.feature.exportsettings.R

@Composable
fun ExportFormatSection(
    format: ExportFormat,
    onFormatChange: (ExportFormat) -> Unit,
) {
    SettingsCard {
        ExportFormatRadioItem(
            label = stringResource(R.string.export_format_image),
            selected = format == ExportFormat.INDIVIDUAL,
            onClick = { onFormatChange(ExportFormat.INDIVIDUAL) },
        )
        SettingsDivider()
        ExportFormatRadioItem(
            label = stringResource(R.string.export_format_zip),
            selected = format == ExportFormat.ZIP,
            onClick = { onFormatChange(ExportFormat.ZIP) },
        )
    }
}

@Composable
private fun ExportFormatRadioItem(
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
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
    }
}
