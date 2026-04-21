package com.pairshot.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pairshot.core.designsystem.PairShotMotionTokens
import com.pairshot.core.navigation.AfterCamera
import com.pairshot.core.navigation.Camera
import com.pairshot.core.navigation.CombineSettings
import com.pairshot.core.navigation.CombinedViewer
import com.pairshot.core.navigation.Compare
import com.pairshot.core.navigation.Export
import com.pairshot.core.navigation.License
import com.pairshot.core.navigation.ProjectDetail
import com.pairshot.core.navigation.ProjectList
import com.pairshot.core.navigation.Settings
import com.pairshot.core.navigation.WatermarkSettings
import com.pairshot.feature.camera.route.AfterCameraRoute
import com.pairshot.feature.camera.route.CameraRoute
import com.pairshot.feature.compare.route.CombinedViewerRoute
import com.pairshot.feature.compare.route.CompareRoute
import com.pairshot.feature.export.route.ExportRoute
import com.pairshot.feature.gallery.route.GalleryRoute
import com.pairshot.feature.project.route.ProjectListRoute
import com.pairshot.feature.settings.route.CombineSettingsRoute
import com.pairshot.feature.settings.route.SettingsRoute
import com.pairshot.feature.settings.route.WatermarkSettingsRoute
import com.pairshot.feature.settings.screen.LicenseScreen

@Composable
fun PairShotNavHost(
    navController: NavHostController = rememberNavController(),
    onDestinationChanged: (String) -> Unit = {},
) {
    DisposableEffect(navController) {
        val listener =
            NavController.OnDestinationChangedListener { _, destination, _ ->
                val route =
                    destination.route?.substringAfterLast(".")?.substringBefore("?")
                        ?: "Unknown"
                onDestinationChanged(route)
            }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    NavHost(
        navController = navController,
        startDestination = ProjectList,
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        enterTransition = {
            fadeIn(animationSpec = PairShotMotionTokens.enterTween())
        },
        exitTransition = {
            fadeOut(animationSpec = PairShotMotionTokens.exitTween())
        },
        popEnterTransition = {
            fadeIn(animationSpec = PairShotMotionTokens.popEnterTween())
        },
        popExitTransition = {
            fadeOut(animationSpec = PairShotMotionTokens.popExitTween())
        },
        sizeTransform = { null },
    ) {
        composable<ProjectList> {
            ProjectListRoute(
                onNavigateToSettings = { navController.navigate(Settings) },
                onNavigateToProject = { projectId -> navController.navigate(ProjectDetail(projectId)) },
            )
        }
        composable<ProjectDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<ProjectDetail>()
            GalleryRoute(
                projectId = route.projectId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(Camera(route.projectId)) },
                onNavigateToAfterCamera = { pairId -> navController.navigate(AfterCamera(route.projectId, pairId)) },
                onNavigateToCompare = { pairId -> navController.navigate(Compare(pairId)) },
                onNavigateToCombined = { pairId -> navController.navigate(CombinedViewer(pairId)) },
                onNavigateToExport = { selectedIds ->
                    navController.navigate(Export(route.projectId, selectedIds.joinToString(",")))
                },
                onNavigateToCombineSettings = { navController.navigate(CombineSettings) },
                onNavigateToWatermarkSettings = { navController.navigate(WatermarkSettings) },
            )
        }
        composable<Camera> { backStackEntry ->
            val route = backStackEntry.toRoute<Camera>()
            CameraRoute(
                projectId = route.projectId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<AfterCamera> { backStackEntry ->
            val route = backStackEntry.toRoute<AfterCamera>()
            AfterCameraRoute(
                projectId = route.projectId,
                initialPairId = route.initialPairId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        dialog<CombinedViewer>(
            dialogProperties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnClickOutside = true,
                ),
        ) {
            CombinedViewerRoute(
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
            CompareRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAfterCamera = { projectId, pairId ->
                    navController.navigate(AfterCamera(projectId, pairId))
                },
            )
        }
        composable<Export> {
            val exportViewModel: com.pairshot.feature.export.viewmodel.ExportViewModel =
                androidx.hilt.navigation.compose
                    .hiltViewModel()
            com.pairshot.app.navigation.effect.ExportShareEffect(
                exportAction = exportViewModel.exportAction,
            )
            ExportRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWatermarkSettings = { navController.navigate(WatermarkSettings) },
                onNavigateToCombineSettings = { navController.navigate(CombineSettings) },
                viewModel = exportViewModel,
            )
        }
        composable<Settings> {
            SettingsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLicense = { navController.navigate(License) },
                onNavigateToWatermarkSettings = { navController.navigate(WatermarkSettings) },
                onNavigateToCombineSettings = { navController.navigate(CombineSettings) },
            )
        }
        composable<License> {
            LicenseScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<WatermarkSettings> {
            WatermarkSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<CombineSettings> {
            CombineSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
