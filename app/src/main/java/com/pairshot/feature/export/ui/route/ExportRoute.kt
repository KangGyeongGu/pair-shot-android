package com.pairshot.feature.export.ui.route

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.app.navigation.effect.ExportShareEffect
import com.pairshot.feature.export.ui.screen.ExportLoadingScreen
import com.pairshot.feature.export.ui.screen.ExportScreen
import com.pairshot.feature.export.ui.viewmodel.ExportFormat
import com.pairshot.feature.export.ui.viewmodel.ExportUiState
import com.pairshot.feature.export.ui.viewmodel.ExportViewModel

@Composable
fun ExportRoute(
    onNavigateBack: () -> Unit,
    onNavigateToWatermarkSettings: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val includeBefore by viewModel.includeBefore.collectAsStateWithLifecycle()
    val includeAfter by viewModel.includeAfter.collectAsStateWithLifecycle()
    val includeCombined by viewModel.includeCombined.collectAsStateWithLifecycle()
    val exportFormat by viewModel.exportFormat.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val applyWatermark by viewModel.applyWatermark.collectAsStateWithLifecycle()

    val safLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/zip"),
        ) { uri: Uri? ->
            uri?.let { viewModel.saveToDevice(it.toString()) }
        }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect("snackbar") {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    ExportShareEffect(exportAction = viewModel.exportAction)

    val onSaveToDevice = {
        when (exportFormat) {
            ExportFormat.ZIP -> {
                val name =
                    (uiState as? ExportUiState.Success)?.projectName ?: "PairShot"
                safLauncher.launch("PairShot_$name.zip")
            }

            ExportFormat.INDIVIDUAL -> {
                viewModel.saveToDevice()
            }
        }
    }

    when (val state = uiState) {
        is ExportUiState.Loading -> {
            ExportLoadingScreen(
                onNavigateBack = onNavigateBack,
                snackbarHostState = snackbarHostState,
            )
        }

        is ExportUiState.Error -> {
            ExportScreen(
                pairCount = 0,
                beforeCount = 0,
                afterCount = 0,
                combinedCount = 0,
                includeBefore = includeBefore,
                includeAfter = includeAfter,
                includeCombined = includeCombined,
                onIncludeBeforeChange = viewModel::setIncludeBefore,
                onIncludeAfterChange = viewModel::setIncludeAfter,
                onIncludeCombinedChange = viewModel::setIncludeCombined,
                exportFormat = exportFormat,
                onExportFormatChange = viewModel::setExportFormat,
                applyWatermark = applyWatermark,
                onApplyWatermarkChange = viewModel::setApplyWatermark,
                onWatermarkSettingsClick = onNavigateToWatermarkSettings,
                onSaveToDevice = onSaveToDevice,
                onShare = viewModel::share,
                onNavigateBack = onNavigateBack,
                snackbarHostState = snackbarHostState,
            )
        }

        is ExportUiState.Success -> {
            ExportScreen(
                pairCount = state.pairCount,
                beforeCount = state.beforeCount,
                afterCount = state.afterCount,
                combinedCount = state.combinedCount,
                incompleteCount = state.incompleteCount,
                includeBefore = includeBefore,
                includeAfter = includeAfter,
                includeCombined = includeCombined,
                onIncludeBeforeChange = viewModel::setIncludeBefore,
                onIncludeAfterChange = viewModel::setIncludeAfter,
                onIncludeCombinedChange = viewModel::setIncludeCombined,
                exportFormat = exportFormat,
                onExportFormatChange = viewModel::setExportFormat,
                applyWatermark = applyWatermark,
                onApplyWatermarkChange = viewModel::setApplyWatermark,
                onWatermarkSettingsClick = onNavigateToWatermarkSettings,
                onSaveToDevice = onSaveToDevice,
                onShare = viewModel::share,
                onNavigateBack = onNavigateBack,
                isExporting = isExporting,
                exportProgress = exportProgress,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}
