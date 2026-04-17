package com.pairshot.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pairshot.ui.camera.CameraScreen
import com.pairshot.ui.compare.CompareScreen
import com.pairshot.ui.export.ExportAction
import com.pairshot.ui.export.ExportFormat
import com.pairshot.ui.export.ExportLoadingScreen
import com.pairshot.ui.export.ExportScreen
import com.pairshot.ui.export.ExportUiState
import com.pairshot.ui.export.ExportViewModel
import com.pairshot.ui.gallery.GalleryScreen
import com.pairshot.ui.pairing.PairingScreen
import com.pairshot.ui.project.ProjectListScreen
import com.pairshot.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
data object ProjectList

@Serializable
data class ProjectDetail(
    val projectId: Long,
)

@Serializable
data class Camera(
    val projectId: Long,
)

@Serializable
data class Pairing(
    val projectId: Long,
    val initialPairId: Long? = null,
)

@Serializable
data class Compare(
    val pairId: Long,
)

@Serializable
data class Export(
    val projectId: Long,
    val pairIds: String,
)

@Serializable
data object Settings

@Composable
fun PairShotNavGraph(navController: NavHostController = rememberNavController()) {
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
                onNavigateToPairing = { pairId -> navController.navigate(Pairing(route.projectId, pairId)) },
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
        composable<Pairing> { backStackEntry ->
            val route = backStackEntry.toRoute<Pairing>()
            PairingScreen(
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
                onNavigateToPairing = { projectId, pairId ->
                    navController.navigate(Pairing(projectId, pairId))
                },
            )
        }
        composable<Export> {
            val viewModel: ExportViewModel = hiltViewModel()
            val context = LocalContext.current
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val includeBefore by viewModel.includeBefore.collectAsStateWithLifecycle()
            val includeAfter by viewModel.includeAfter.collectAsStateWithLifecycle()
            val includeCombined by viewModel.includeCombined.collectAsStateWithLifecycle()
            val exportFormat by viewModel.exportFormat.collectAsStateWithLifecycle()
            val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
            val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()

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

            LaunchedEffect("exportAction") {
                viewModel.exportAction.collect { action ->
                    val resolver = context.contentResolver
                    val intent =
                        when (action) {
                            is ExportAction.ShareZip -> {
                                val zipFile = java.io.File(action.filePath)
                                val authority = "${context.packageName}.fileprovider"
                                val uri = FileProvider.getUriForFile(context, authority, zipFile)
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "application/zip"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    clipData =
                                        android.content.ClipData.newUri(
                                            resolver,
                                            "PairShot ZIP",
                                            uri,
                                        )
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            }

                            is ExportAction.ShareImages -> {
                                val uris = action.uris.map { Uri.parse(it) }
                                if (uris.size == 1) {
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, uris.first())
                                        clipData =
                                            android.content.ClipData.newUri(
                                                resolver,
                                                "PairShot",
                                                uris.first(),
                                            )
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                } else {
                                    Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                        type = "image/*"
                                        putParcelableArrayListExtra(
                                            Intent.EXTRA_STREAM,
                                            ArrayList(uris),
                                        )
                                        clipData =
                                            android.content.ClipData
                                                .newUri(resolver, "PairShot", uris.first())
                                                .apply {
                                                    uris.drop(1).forEach {
                                                        addItem(
                                                            android.content.ClipData.Item(it),
                                                        )
                                                    }
                                                }
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                }
                            }
                        }
                    context.startActivity(Intent.createChooser(intent, null))
                }
            }

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
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
