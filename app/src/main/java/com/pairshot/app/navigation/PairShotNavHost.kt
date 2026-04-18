package com.pairshot.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pairshot.app.navigation.route.AfterCamera
import com.pairshot.app.navigation.route.Camera
import com.pairshot.app.navigation.route.Compare
import com.pairshot.app.navigation.route.Export
import com.pairshot.app.navigation.route.License
import com.pairshot.app.navigation.route.ProjectDetail
import com.pairshot.app.navigation.route.ProjectList
import com.pairshot.app.navigation.route.Settings
import com.pairshot.app.navigation.route.WatermarkSettings
import com.pairshot.core.designsystem.PairShotMotionTokens
import com.pairshot.feature.camera.ui.route.AfterCameraRoute
import com.pairshot.feature.camera.ui.route.CameraRoute
import com.pairshot.feature.compare.ui.route.CompareRoute
import com.pairshot.feature.export.ui.route.ExportRoute
import com.pairshot.feature.pair.ui.route.GalleryRoute
import com.pairshot.feature.project.ui.route.ProjectListRoute
import com.pairshot.feature.settings.ui.route.SettingsRoute
import com.pairshot.feature.settings.ui.route.WatermarkSettingsRoute
import com.pairshot.feature.settings.ui.screen.LicenseScreen

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
                .background(Color.Black),
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
                onNavigateToExport = { selectedIds ->
                    navController.navigate(Export(route.projectId, selectedIds.joinToString(",")))
                },
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
            ExportRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWatermarkSettings = { navController.navigate(WatermarkSettings) },
            )
        }
        composable<Settings> {
            SettingsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLicense = { navController.navigate(License) },
                onNavigateToWatermarkSettings = { navController.navigate(WatermarkSettings) },
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
    }
}
