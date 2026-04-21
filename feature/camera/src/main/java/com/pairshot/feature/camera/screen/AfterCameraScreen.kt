package com.pairshot.feature.camera.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.core.ui.component.PairShotSnackbar
import com.pairshot.core.ui.component.PairShotSnackbarController
import com.pairshot.core.ui.component.SnackbarEvent
import com.pairshot.core.ui.component.SnackbarVariant
import com.pairshot.feature.camera.chrome.AfterCameraTopBar
import com.pairshot.feature.camera.chrome.CameraBottomBar
import com.pairshot.feature.camera.component.BeforePreviewStrip
import com.pairshot.feature.camera.component.CameraSettingsSheet
import com.pairshot.feature.camera.component.OverlayGuide
import com.pairshot.feature.camera.preview.CameraPreviewPane
import com.pairshot.feature.camera.viewmodel.AfterCameraEvent
import com.pairshot.feature.camera.viewmodel.AfterCameraViewModel
import kotlinx.coroutines.delay

private val CameraTopBarHeight = 56.dp
private val CameraStripHeight = 120.dp
private val CameraShutterHeight = 116.dp

@Composable
internal fun AfterCameraScreen(
    viewModel: AfterCameraViewModel,
    onNavigateBack: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val unpairedPhotos by viewModel.unpairedPhotos.collectAsStateWithLifecycle()
    val totalPairCount by viewModel.totalPairCount.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    val zoomUiState by viewModel.zoomUiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val overlayEnabled by viewModel.overlayEnabled.collectAsStateWithLifecycle()
    val overlayAlpha by viewModel.overlayAlpha.collectAsStateWithLifecycle()
    val overlayBitmap by viewModel.overlayBitmap.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilities.collectAsStateWithLifecycle()
    val roll by viewModel.roll.collectAsStateWithLifecycle()
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()

    val currentPair = unpairedPhotos.getOrNull(currentIndex)
    val totalCount = unpairedPhotos.size
    val completedCount = (totalPairCount - totalCount).coerceAtLeast(0)
    val beforePreviewUris = unpairedPhotos.map { it.beforePhotoUri }

    val snackbarController = remember { PairShotSnackbarController() }
    val thumbnailListState = rememberLazyListState()

    LaunchedEffect(lifecycleOwner) {
        viewModel.bind(lifecycleOwner)
    }

    var showBlackout by remember { mutableStateOf(false) }
    val blackoutAlpha by animateFloatAsState(
        targetValue = if (showBlackout) 0.6f else 0f,
        animationSpec = tween(durationMillis = if (showBlackout) 30 else 100),
        label = "capture_blackout",
        finishedListener = { if (showBlackout) showBlackout = false },
    )

    LaunchedEffect(unpairedPhotos) {
        viewModel.onUnpairedPhotosUpdated(unpairedPhotos)
        if (unpairedPhotos.isEmpty()) {
            viewModel.emitAllCompleted()
        }
    }

    LaunchedEffect(currentIndex) {
        if (unpairedPhotos.isNotEmpty()) {
            val index = currentIndex.coerceIn(0, unpairedPhotos.lastIndex)
            thumbnailListState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(currentIndex, unpairedPhotos) {
        val pair = unpairedPhotos.getOrNull(currentIndex) ?: return@LaunchedEffect
        viewModel.restoreZoomForPair(pair.zoomLevel)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AfterCameraEvent.AfterSaved -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                is AfterCameraEvent.AllCompleted -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    snackbarController.show(
                        SnackbarEvent("모든 Pair 촬영이 완료되었습니다.", SnackbarVariant.SUCCESS),
                    )
                    delay(2000L)
                    onNavigateBack()
                }

                is AfterCameraEvent.CaptureError -> {
                    snackbarController.show(
                        SnackbarEvent("촬영에 실패했습니다. 다시 시도해주세요.", SnackbarVariant.ERROR),
                    )
                }

                is AfterCameraEvent.SaveError -> {
                    snackbarController.show(
                        SnackbarEvent("오류", SnackbarVariant.ERROR),
                    )
                }
            }
        }
    }

    val density = LocalDensity.current
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val safeTopPx = WindowInsets.safeDrawing.getTop(density)
        val safeBottomPx = WindowInsets.safeDrawing.getBottom(density)
        val fullHeightPx = with(density) { maxHeight.roundToPx() }
        val safeAvailableHeightPx = (fullHeightPx - safeTopPx - safeBottomPx).coerceAtLeast(0)
        val safeAvailableHeightDp = with(density) { safeAvailableHeightPx.toDp() }
        val topSectionHeight = CameraTopBarHeight
        val stripSectionHeight = CameraStripHeight
        val shutterSectionHeight = CameraShutterHeight
        val bottomSpacerDesired = 32.dp
        val minPreviewHeight = 180.dp

        val reservedHeight = topSectionHeight + stripSectionHeight + shutterSectionHeight + bottomSpacerDesired
        val previewHeightRaw = safeAvailableHeightDp - reservedHeight
        val previewSectionHeight = if (previewHeightRaw >= minPreviewHeight) previewHeightRaw else minPreviewHeight
        val bottomSpacerHeight =
            if (previewHeightRaw >= minPreviewHeight) {
                bottomSpacerDesired
            } else {
                (safeAvailableHeightDp - (topSectionHeight + stripSectionHeight + shutterSectionHeight + previewSectionHeight))
                    .coerceAtLeast(0.dp)
            }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                AfterCameraTopBar(
                    onNavigateBack = onNavigateBack,
                    completedCount = completedCount,
                    totalPairCount = totalPairCount,
                    height = topSectionHeight,
                )

                CameraPreviewPane(
                    surfaceRequest = surfaceRequest,
                    zoomUiState = zoomUiState,
                    blackoutAlpha = blackoutAlpha,
                    gridEnabled = settingsState.gridEnabled,
                    levelEnabled = settingsState.levelEnabled,
                    roll = roll,
                    exposureIndexMin = capabilities.exposureIndexMin,
                    exposureIndexMax = capabilities.exposureIndexMax,
                    currentExposureIndex = settingsState.exposureIndex,
                    exposureStepNumerator = capabilities.exposureStepNumerator,
                    exposureStepDenominator = capabilities.exposureStepDenominator,
                    height = previewSectionHeight,
                    onZoomRatioChanged = { newRatio -> viewModel.updateZoomRatio(newRatio) },
                    onPresetTapped = { preset -> viewModel.onPresetTapped(preset) },
                    onDragEnd = { viewModel.applyCustomRatio() },
                    onExposureReset = { viewModel.setExposureIndex(0) },
                    onExposureAdjust = { index -> viewModel.setExposureIndex(index) },
                    onTapToFocus = { x, y, w, h ->
                        viewModel.onFocusRequested(x, y, w.toFloat(), h.toFloat())
                    },
                    overlayContent = {
                        if (overlayEnabled) {
                            OverlayGuide(
                                bitmap = overlayBitmap,
                                alpha = overlayAlpha,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    },
                )

                BeforePreviewStrip(
                    beforePreviewUris = beforePreviewUris,
                    modifier = Modifier.height(stripSectionHeight),
                    selectedIndex = if (totalCount > 0) currentIndex else null,
                    onSelectIndex = viewModel::selectIndex,
                    listState = thumbnailListState,
                    emptyMessage = "촬영할 Before가 없습니다",
                    stripHeight = stripSectionHeight,
                )

                CameraBottomBar(
                    isSaving = isSaving,
                    shutterEnabled = currentPair != null,
                    height = shutterSectionHeight,
                    onToggleLens = { viewModel.toggleLensFacing() },
                    onToggleSettings = { viewModel.toggleSettingsPanel() },
                    onShutterClick = {
                        showBlackout = true
                        viewModel.onAfterCaptured()
                    },
                )

                Spacer(modifier = Modifier.height(bottomSpacerHeight))
            }

            CameraSettingsSheet(
                visible = settingsState.showPanel,
                settingsState = settingsState,
                capabilities = capabilities,
                onToggleGrid = viewModel::toggleGrid,
                onCycleFlash = viewModel::cycleFlash,
                onToggleNightMode = viewModel::toggleNightMode,
                onToggleHdr = viewModel::toggleHdr,
                onToggleLevel = viewModel::toggleLevel,
                onDismiss = viewModel::dismissSettingsPanel,
                overlayEnabled = overlayEnabled,
                onToggleOverlay = viewModel::toggleOverlay,
                overlayAlpha = overlayAlpha,
                onOverlayAlphaChange = viewModel::updateOverlayAlpha,
            )

            SnackbarHost(
                hostState = snackbarController.hostState,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 8.dp),
                snackbar = { data ->
                    PairShotSnackbar(
                        message = data.visuals.message,
                        variant = snackbarController.currentVariant,
                        actionLabel = data.visuals.actionLabel,
                        onAction = { data.performAction() },
                    )
                },
            )
        }
    }
}
