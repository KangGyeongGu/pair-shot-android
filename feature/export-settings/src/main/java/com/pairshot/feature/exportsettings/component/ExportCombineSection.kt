package com.pairshot.feature.exportsettings.component

import androidx.compose.runtime.Composable
import com.pairshot.core.ui.component.SettingsCard

@Composable
fun ExportCombineSection(
    applyCombineConfig: Boolean,
    onApplyCombineConfigChange: (Boolean) -> Unit,
    onNavigateToCombineSettings: () -> Unit,
) {
    SettingsCard {
        ExportSwitchWithGearItem(
            label = "합성 옵션 적용",
            checked = applyCombineConfig,
            onCheckedChange = onApplyCombineConfigChange,
            onGearClick = onNavigateToCombineSettings,
        )
    }
}
