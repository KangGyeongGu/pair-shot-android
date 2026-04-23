package com.pairshot.feature.exportsettings.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pairshot.core.ui.component.SettingsCard
import com.pairshot.feature.exportsettings.R

@Composable
fun ExportCombineSection(
    applyCombineConfig: Boolean,
    onApplyCombineConfigChange: (Boolean) -> Unit,
    onNavigateToCombineSettings: () -> Unit,
) {
    SettingsCard {
        ExportSwitchWithGearItem(
            label = stringResource(R.string.export_combine_apply),
            checked = applyCombineConfig,
            onCheckedChange = onApplyCombineConfigChange,
            onGearClick = onNavigateToCombineSettings,
        )
    }
}
