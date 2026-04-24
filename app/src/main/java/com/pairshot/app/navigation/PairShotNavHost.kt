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
import com.pairshot.core.coupon.ui.CouponQrScannerScreen
import com.pairshot.core.designsystem.PairShotMotionTokens
import com.pairshot.core.navigation.AfterCamera
import com.pairshot.core.navigation.AlbumDetail
import com.pairshot.core.navigation.Camera
import com.pairshot.core.navigation.CombineSettings
import com.pairshot.core.navigation.CouponQrScanner
import com.pairshot.core.navigation.ExportSettings
import com.pairshot.core.navigation.Home
import com.pairshot.core.navigation.License
import com.pairshot.core.navigation.PairPicker
import com.pairshot.core.navigation.PairPreview
import com.pairshot.core.navigation.Settings
import com.pairshot.core.navigation.WatermarkSettings
import com.pairshot.feature.album.route.AlbumDetailRoute
import com.pairshot.feature.album.route.PairPickerRoute
import com.pairshot.feature.camera.route.AfterCameraRoute
import com.pairshot.feature.camera.route.CameraRoute
import com.pairshot.feature.exportsettings.route.ExportSettingsRoute
import com.pairshot.feature.home.route.HomeRoute
import com.pairshot.feature.pairpreview.route.PairPreviewRoute
import com.pairshot.feature.settings.route.CombineSettingsRoute
import com.pairshot.feature.settings.route.SettingsRoute
import com.pairshot.feature.settings.route.WatermarkSettingsRoute
import com.pairshot.feature.settings.screen.LicenseScreen

private const val COUPON_CODE_RESULT_KEY = "coupon_code_result"

@Composable
fun PairShotNavHost(
    navController: NavHostController = rememberNavController(),
    onDestinationChanged: (String) -> Unit = {},
    onShareSelected: (Set<Long>) -> Unit = {},
    onSaveSelectedToDevice: (Set<Long>) -> Unit = {},
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
        startDestination = Camera(),
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
        composable<Home> {
            HomeRoute(
                onNavigateToPairPreview = { pairId -> navController.navigate(PairPreview(pairId)) },
                onNavigateToAfterCamera = { pairId ->
                    navController.navigate(AfterCamera(initialPairId = pairId))
                },
                onNavigateToAlbumDetail = { albumId -> navController.navigate(AlbumDetail(albumId)) },
                onNavigateToCamera = { navController.navigate(Camera()) },
                onNavigateToSettings = { navController.navigate(Settings) },
                onNavigateToExportSettings = { ids ->
                    navController.navigate(ExportSettings(ids.joinToString(",")))
                },
                onShareSelected = onShareSelected,
                onSaveToDevice = onSaveSelectedToDevice,
            )
        }
        composable<AlbumDetail> {
            AlbumDetailRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPairPreview = { pairId -> navController.navigate(PairPreview(pairId)) },
                onNavigateToAfterCamera = { pairId, albumId ->
                    navController.navigate(AfterCamera(initialPairId = pairId, albumId = albumId))
                },
                onNavigateToCamera = { albumId ->
                    navController.navigate(Camera(albumId = albumId))
                },
                onNavigateToPairPicker = { albumId -> navController.navigate(PairPicker(albumId)) },
                onNavigateToExportSettings = { ids ->
                    navController.navigate(ExportSettings(ids.joinToString(",")))
                },
                onShareSelected = onShareSelected,
                onSaveSelectedToDevice = onSaveSelectedToDevice,
            )
        }
        composable<PairPicker> {
            PairPickerRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<Camera> {
            CameraRoute(
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Home) {
                            popUpTo<Camera> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        composable<AfterCamera> {
            AfterCameraRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        dialog<PairPreview>(
            dialogProperties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnClickOutside = true,
                ),
        ) {
            PairPreviewRoute(
                onDismiss = { navController.popBackStack() },
                onShareSelected = { pairId -> onShareSelected(setOf(pairId)) },
                onNavigateToAfterCamera = { pairId ->
                    navController.navigate(AfterCamera(initialPairId = pairId))
                },
            )
        }
        composable<ExportSettings> {
            ExportSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWatermarkSettings = { navController.navigate(WatermarkSettings) },
                onNavigateToCombineSettings = { navController.navigate(CombineSettings) },
            )
        }
        composable<Settings> { entry ->
            val pendingCouponCode =
                entry.savedStateHandle.get<String>(COUPON_CODE_RESULT_KEY)
            SettingsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLicense = { navController.navigate(License) },
                onNavigateToWatermarkSettings = { navController.navigate(WatermarkSettings) },
                onNavigateToCombineSettings = { navController.navigate(CombineSettings) },
                onNavigateToCouponScanner = { navController.navigate(CouponQrScanner) },
                pendingCouponCode = pendingCouponCode,
                onConsumeCouponCode = {
                    entry.savedStateHandle.remove<String>(COUPON_CODE_RESULT_KEY)
                },
            )
        }
        composable<CouponQrScanner> {
            CouponQrScannerScreen(
                onResult = { value ->
                    navController
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(COUPON_CODE_RESULT_KEY, value)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() },
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
