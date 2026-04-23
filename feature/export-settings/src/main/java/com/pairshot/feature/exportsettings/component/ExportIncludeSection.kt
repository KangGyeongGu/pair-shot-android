package com.pairshot.feature.exportsettings.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pairshot.core.ui.component.SettingsCard
import com.pairshot.core.ui.component.SettingsDivider
import com.pairshot.core.ui.component.SettingsSwitchItem
import com.pairshot.feature.exportsettings.R

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
            label = stringResource(R.string.export_include_combined_only),
            checked = includeCombined,
            onCheckedChange = onIncludeCombinedChange,
        )
        SettingsDivider()
        SettingsSwitchItem(
            label = stringResource(R.string.export_include_before_only),
            checked = includeBefore,
            onCheckedChange = onIncludeBeforeChange,
        )
        SettingsDivider()
        SettingsSwitchItem(
            label = stringResource(R.string.export_include_after_only),
            checked = includeAfter,
            onCheckedChange = onIncludeAfterChange,
        )
    }
}
