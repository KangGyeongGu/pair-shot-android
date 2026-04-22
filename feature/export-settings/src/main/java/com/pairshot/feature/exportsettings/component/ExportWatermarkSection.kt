package com.pairshot.feature.exportsettings.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.ui.component.SettingsCard

@Composable
fun ExportWatermarkSection(
    applyWatermark: Boolean,
    onApplyWatermarkChange: (Boolean) -> Unit,
    onNavigateToWatermarkSettings: () -> Unit,
) {
    SettingsCard {
        ExportSwitchWithGearItem(
            label = "워터마크 옵션 적용",
            checked = applyWatermark,
            onCheckedChange = onApplyWatermarkChange,
            onGearClick = onNavigateToWatermarkSettings,
        )
    }
}

@Composable
internal fun ExportSwitchWithGearItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onGearClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(PairShotSpacing.inputRow)
                .padding(start = PairShotSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(it)
            },
            colors =
                SwitchDefaults.colors(
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            modifier =
                Modifier
                    .wrapContentHeight(unbounded = true)
                    .scale(0.67f),
        )
        IconButton(onClick = onGearClick) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "설정",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
