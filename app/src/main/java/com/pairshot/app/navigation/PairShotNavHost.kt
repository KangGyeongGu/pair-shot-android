package com.pairshot.app.navigation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pairshot.app.navigation.effect.ExportShareEffect
import com.pairshot.app.navigation.route.AfterCamera
import com.pairshot.app.navigation.route.Camera
import com.pairshot.app.navigation.route.Compare
import com.pairshot.app.navigation.route.Export
import com.pairshot.app.navigation.route.License
import com.pairshot.app.navigation.route.ProjectDetail
import com.pairshot.app.navigation.route.ProjectList
import com.pairshot.app.navigation.route.Settings
import com.pairshot.app.navigation.route.WatermarkSettings
import com.pairshot.ui.aftercamera.AfterCameraScreen
import com.pairshot.ui.camera.CameraScreen
import com.pairshot.ui.compare.CompareScreen
import com.pairshot.ui.export.ExportFormat
import com.pairshot.ui.export.ExportLoadingScreen
import com.pairshot.ui.export.ExportScreen
import com.pairshot.ui.export.ExportUiState
import com.pairshot.ui.export.ExportViewModel
import com.pairshot.ui.gallery.GalleryScreen
import com.pairshot.ui.project.ProjectListScreen
import com.pairshot.ui.settings.LicenseScreen
import com.pairshot.ui.settings.SettingsScreen
import com.pairshot.ui.settings.SettingsViewModel
import com.pairshot.ui.settings.WatermarkSettingsScreen

@Composable
fun PairShotNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = ProjectList,
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
        enterTransition = {
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = 220,
                        delayMillis = 130,
                        easing = LinearOutSlowInEasing,
                    ),
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec =
                    tween(
                        durationMillis = 180,
                        easing = FastOutLinearInEasing,
                    ),
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = 210,
                        delayMillis = 120,
                        easing = LinearOutSlowInEasing,
                    ),
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec =
                    tween(
                        durationMillis = 170,
                        easing = FastOutLinearInEasing,
                    ),
            )
        },
        sizeTransform = { null },
    ) {
        composable<ProjectList> {
            ProjectListScreen(
                onNavigateToSettings = { navController.navigate(Settings) },
                onNavigateToProject = { projectId -> navController.navigate(ProjectDetail(projectId)) },
            )
        }
        composable<ProjectDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<ProjectDetail>()
            GalleryScreen(
                projectId = route.projectId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(Camera(route.projectId)) },
                onNavigateToAfterCamera = { pairId -> navController.navigate(AfterCamera(route.projectId, pairId)) },
                onNavigateToCompare = { pairId -> navController.navigate(Compare(pairId)) },
                onNavigateToExport = { selectedIds ->
                    navController.navigate(Export(route.projectId, selectedIds.joinToString(",")))
                },
            )
        }
        composable<Camera> { backStackEntry ->
            val route = backStackEntry.toRoute<Camera>()
            CameraScreen(
                projectId = route.projectId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<AfterCamera> { backStackEntry ->
            val route = backStackEntry.toRoute<AfterCamera>()
            AfterCameraScreen(
                projectId = route.projectId,
                initialPairId = route.initialPairId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        dialog<Compare>(
            dialogProperties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnClickOutside = true,
                ),
        ) {
            CompareScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAfterCamera = { projectId, pairId ->
                    navController.navigate(AfterCamera(projectId, pairId))
                },
            )
        }
        composable<Export> {
            val viewModel: ExportViewModel = hiltViewModel()
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
                        onNavigateBack = { navController.popBackStack() },
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
                        onWatermarkSettingsClick = { navController.navigate(WatermarkSettings) },
                        onSaveToDevice = onSaveToDevice,
                        onShare = viewModel::share,
                        onNavigateBack = { navController.popBackStack() },
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
                        onWatermarkSettingsClick = { navController.navigate(WatermarkSettings) },
                        onSaveToDevice = onSaveToDevice,
                        onShare = viewModel::share,
                        onNavigateBack = { navController.popBackStack() },
                        isExporting = isExporting,
                        exportProgress = exportProgress,
                        snackbarHostState = snackbarHostState,
                    )
                }
            }
        }
        composable<Settings> {
            val viewModel: SettingsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val watermarkConfig by viewModel.watermarkConfig.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }
            val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

            LaunchedEffect(lifecycleOwner) {
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.refresh()
                }
            }

            LaunchedEffect("snackbar") {
                viewModel.snackbarMessage.collect { message ->
                    snackbarHostState.showSnackbar(message)
                }
            }

            SettingsScreen(
                uiState = uiState,
                watermarkConfig = watermarkConfig,
                onClearCache = viewModel::clearCache,
                onLicenseClick = { navController.navigate(License) },
                onNavigateBack = { navController.popBackStack() },
                onWatermarkConfigChange = viewModel::updateWatermarkConfig,
                onWatermarkSettingsClick = { navController.navigate(WatermarkSettings) },
                onJpegQualityChange = viewModel::updateJpegQuality,
                onFileNamePrefixChange = viewModel::updateFileNamePrefix,
                onOverlayEnabledChange = viewModel::updateOverlayEnabled,
                onOverlayAlphaChange = viewModel::updateOverlayAlpha,
                snackbarHostState = snackbarHostState,
            )
        }
        composable<License> {
            LicenseScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<WatermarkSettings> {
            val viewModel: SettingsViewModel = hiltViewModel()
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
                onNavigateBack = { navController.popBackStack() },
                watermarkManager = viewModel.watermarkManager,
            )
        }
    }
}
