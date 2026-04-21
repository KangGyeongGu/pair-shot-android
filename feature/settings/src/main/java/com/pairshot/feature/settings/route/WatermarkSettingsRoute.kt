package com.pairshot.feature.settings.route

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.feature.settings.screen.WatermarkSettingsScreen
import com.pairshot.feature.settings.viewmodel.SettingsViewModel

@Composable
fun WatermarkSettingsRoute(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val watermarkConfig by viewModel.watermarkConfig.collectAsStateWithLifecycle()

    val logoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri: Uri? ->
            uri?.let { viewModel.saveLogoFile(it.toString()) }
        }

    WatermarkSettingsScreen(
        watermarkConfig = watermarkConfig,
        onWatermarkConfigChange = viewModel::updateWatermarkConfig,
        onSelectLogo = { logoPickerLauncher.launch(arrayOf("image/*")) },
        onNavigateBack = onNavigateBack,
        watermarkRenderer = viewModel.watermarkRenderer,
        previewSampleProvider = viewModel.previewSampleProvider,
    )
}
