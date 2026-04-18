package com.pairshot.feature.settings.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.pairshot.core.ui.component.PairShotSnackbarController
import com.pairshot.feature.settings.ui.screen.SettingsScreen
import com.pairshot.feature.settings.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToLicense: () -> Unit,
    onNavigateToWatermarkSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val watermarkConfig by viewModel.watermarkConfig.collectAsStateWithLifecycle()
    val snackbarController = remember { PairShotSnackbarController() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh()
        }
    }

    LaunchedEffect("snackbar") {
        viewModel.snackbarMessage.collect { event ->
            snackbarController.show(event)
        }
    }

    SettingsScreen(
        uiState = uiState,
        watermarkConfig = watermarkConfig,
        onClearCache = viewModel::clearCache,
        onLicenseClick = onNavigateToLicense,
        onNavigateBack = onNavigateBack,
        onWatermarkConfigChange = viewModel::updateWatermarkConfig,
        onWatermarkSettingsClick = onNavigateToWatermarkSettings,
        onJpegQualityChange = viewModel::updateJpegQuality,
        onFileNamePrefixChange = viewModel::updateFileNamePrefix,
        onOverlayEnabledChange = viewModel::updateOverlayEnabled,
        onOverlayAlphaChange = viewModel::updateOverlayAlpha,
        snackbarController = snackbarController,
    )
}
