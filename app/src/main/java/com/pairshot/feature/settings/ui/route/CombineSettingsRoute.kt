package com.pairshot.feature.settings.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.feature.settings.ui.screen.CombineSettingsScreen
import com.pairshot.feature.settings.ui.viewmodel.CombineSettingsViewModel

@Composable
fun CombineSettingsRoute(
    onNavigateBack: () -> Unit,
    viewModel: CombineSettingsViewModel = hiltViewModel(),
) {
    val combineConfig by viewModel.combineConfig.collectAsStateWithLifecycle()
    val watermarkConfig by viewModel.watermarkConfig.collectAsStateWithLifecycle()
    CombineSettingsScreen(
        combineConfig = combineConfig,
        watermarkConfig = watermarkConfig,
        watermarkRenderer = viewModel.watermarkRenderer,
        onCombineConfigChange = viewModel::updateCombineConfig,
        onNavigateBack = onNavigateBack,
    )
}
