package com.pairshot.feature.exportsettings.component

import androidx.compose.runtime.Composable
import com.pairshot.core.ui.component.SettingsCard
import com.pairshot.core.ui.component.SettingsDivider
import com.pairshot.core.ui.component.SettingsSwitchItem

@Composable
fun ExportIncludeSection(
    includeBefore: Boolean,
    includeAfter: Boolean,
    includeCombined: Boolean,
    onIncludeBeforeChange: (Boolean) -> Unit,
    onIncludeAfterChange: (Boolean) -> Unit,
    onIncludeCombinedChange: (Boolean) -> Unit,
) {
    SettingsCard {
        SettingsSwitchItem(
            label = "합성본만",
            checked = includeCombined,
            onCheckedChange = onIncludeCombinedChange,
        )
        SettingsDivider()
        SettingsSwitchItem(
            label = "Before만",
            checked = includeBefore,
            onCheckedChange = onIncludeBeforeChange,
        )
        SettingsDivider()
        SettingsSwitchItem(
            label = "After만",
            checked = includeAfter,
            onCheckedChange = onIncludeAfterChange,
        )
    }
}
