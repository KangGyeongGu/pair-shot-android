package com.pairshot.feature.exportsettings.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.feature.exportsettings.screen.ExportSettingsScreen
import com.pairshot.feature.exportsettings.viewmodel.ExportSettingsViewModel

@Composable
fun ExportSettingsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToWatermarkSettings: () -> Unit,
    onNavigateToCombineSettings: () -> Unit,
    viewModel: ExportSettingsViewModel = hiltViewModel(),
) {
    val preset by viewModel.preset.collectAsStateWithLifecycle()
    val applyWatermark by viewModel.applyWatermark.collectAsStateWithLifecycle()
    val applyCombineConfig by viewModel.applyCombineConfig.collectAsStateWithLifecycle()

    ExportSettingsScreen(
        includeBefore = preset.includeBefore,
        includeAfter = preset.includeAfter,
        includeCombined = preset.includeCombined,
        format = preset.format,
        applyWatermark = applyWatermark,
        applyCombineConfig = applyCombineConfig,
        onIncludeBeforeChange = viewModel::setIncludeBefore,
        onIncludeAfterChange = viewModel::setIncludeAfter,
        onIncludeCombinedChange = viewModel::setIncludeCombined,
        onFormatChange = viewModel::setFormat,
        onApplyWatermarkChange = viewModel::setApplyWatermark,
        onApplyCombineConfigChange = viewModel::setApplyCombineConfig,
        onNavigateBack = onNavigateBack,
        onNavigateToWatermarkSettings = onNavigateToWatermarkSettings,
        onNavigateToCombineSettings = onNavigateToCombineSettings,
    )
}
