package com.pairshot.app.navigation

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
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
import com.pairshot.feature.compare.ui.route.CompareRoute
import com.pairshot.feature.export.ui.route.ExportRoute
import com.pairshot.feature.pair.ui.route.GalleryRoute
import com.pairshot.feature.project.ui.route.ProjectListRoute
import com.pairshot.feature.settings.ui.route.SettingsRoute
import com.pairshot.feature.settings.ui.route.WatermarkSettingsRoute
import com.pairshot.feature.settings.ui.screen.LicenseScreen
import com.pairshot.ui.aftercamera.AfterCameraScreen
import com.pairshot.ui.camera.CameraScreen

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
