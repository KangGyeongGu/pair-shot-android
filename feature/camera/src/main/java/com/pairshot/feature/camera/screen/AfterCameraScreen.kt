package com.pairshot.feature.camera.screen

import android.graphics.Bitmap
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.core.ads.component.PairShotBannerAd
import com.pairshot.core.designsystem.PairShotCameraTokens
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.ui.component.PairShotSnackbarController
import com.pairshot.core.ui.component.PairShotSnackbarHost
import com.pairshot.core.ui.component.SnackbarEvent
import com.pairshot.core.ui.component.SnackbarVariant
import com.pairshot.core.ui.text.UiText
import com.pairshot.feature.camera.R
import com.pairshot.feature.camera.chrome.CameraBottomBar
import com.pairshot.feature.camera.component.BeforePreviewStrip
import com.pairshot.feature.camera.component.BeforeStripHeight
import com.pairshot.feature.camera.component.CameraSettingsSheet
import com.pairshot.feature.camera.component.OverlayGuide
import com.pairshot.feature.camera.component.StripProgress
import com.pairshot.feature.camera.preview.CameraPreviewPane
import com.pairshot.feature.camera.viewmodel.AfterCameraEvent
import com.pairshot.feature.camera.viewmodel.AfterCameraViewModel
import com.pairshot.feature.camera.viewmodel.CameraSessionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.pairshot.core.ui.R as CoreR

private val CameraShutterHeight = 116.dp
private const val ALL_COMPLETED_NAVIGATE_BACK_DELAY_MS = 2000L

@Composable
internal fun AfterCameraScreen(
    viewModel: AfterCameraViewModel,
    onNavigateBack: () -> Unit,
    sessionViewModel: CameraSessionViewModel = hiltViewModel(),
) {
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraSession = sessionViewModel.cameraSession
    val sensorSession = sessionViewModel.sensorSession

    val unpairedPhotos by viewModel.unpairedPhotos.collectAsStateWithLifecycle()
    val pairsLoaded by viewModel.pairsLoaded.collectAsStateWithLifecycle()
    val totalPairCount by viewModel.totalPairCount.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val lastPairThumbnailUri by viewModel.lastPairThumbnailUri.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    val zoomUiState by viewModel.zoomUiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val overlayEnabled by viewModel.overlayEnabled.collectAsStateWithLifecycle()
    val overlayAlpha by viewModel.overlayAlpha.collectAsStateWithLifecycle()
    val overlayInputs by viewModel.overlayInputs.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val capabilities by cameraSession.capabilities.collectAsStateWithLifecycle()
    val roll by sensorSession.roll.collectAsStateWithLifecycle()
    val surfaceRequest by cameraSession.surfaceRequest.collectAsStateWithLifecycle()

    val currentPair = unpairedPhotos.getOrNull(currentIndex)
    val totalCount = unpairedPhotos.size
    val completedCount = (totalPairCount - totalCount).coerceAtLeast(0)
    val beforePreviewUris = unpairedPhotos.map { it.beforePhotoUri }

    val snackbarController = remember { PairShotSnackbarController() }
    val thumbnailListState = rememberLazyListState()

    var overlayBitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(overlayInputs) {
        val inputs = overlayInputs
        val pair = inputs.pair
        val next: Bitmap? =
            if (pair == null || !inputs.enabled || inputs.alpha <= 0f) {
                null
            } else {
                cameraSession.prepareOverlay(pair.beforePhotoUri, inputs.lensFacing)?.bitmap
            }
        val previous = overlayBitmap
        overlayBitmap = next
        if (previous != null && previous !== next && !previous.isRecycled) {
            previous.recycle()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            overlayBitmap?.takeIf { !it.isRecycled }?.recycle()
            overlayBitmap = null
        }
    }

    LaunchedEffect(lifecycleOwner) {
        val initial = viewModel.loadInitialSettings()
        cameraSession.setFlash(initial.flashMode)
        cameraSession.setNightMode(initial.nightModeEnabled)
        cameraSession.setHdrMode(initial.hdrEnabled)
        sensorSession.bind(lifecycleOwner)
        cameraSession.bind(lifecycleOwner)
    }

    LaunchedEffect(cameraSession) {
        var capabilitiesInitialized = false
        cameraSession.zoomState.collect { zoom ->
            viewModel.onCameraZoomCapabilities(zoom.min, zoom.max)
            if (!capabilitiesInitialized && zoom.max > zoom.min) {
                capabilitiesInitialized = true
                val pair = viewModel.unpairedPhotos.value.getOrNull(viewModel.currentIndex.value)
                if (pair?.zoomLevel != null) {
                    viewModel.restoreZoomForPair(pair.zoomLevel)
                    cameraSession.setZoom(viewModel.zoomUiState.value.currentRatio)
                }
            }
        }
    }

    LaunchedEffect(capabilities) {
        val adjustment = viewModel.adjustForCapabilities(capabilities)
        adjustment.flashMode?.let { cameraSession.setFlash(it) }
        adjustment.nightModeEnabled?.let { cameraSession.setNightMode(it) }
        adjustment.hdrEnabled?.let { cameraSession.setHdrMode(it) }
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
        if (pairsLoaded && unpairedPhotos.isEmpty()) {
            viewModel.emitAllCompleted()
        }
    }

    LaunchedEffect(currentIndex) {
        if (unpairedPhotos.isNotEmpty() && !thumbnailListState.isScrollInProgress) {
            val index = currentIndex.coerceIn(0, unpairedPhotos.lastIndex)
            thumbnailListState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(currentIndex, unpairedPhotos) {
        val pair = unpairedPhotos.getOrNull(currentIndex) ?: return@LaunchedEffect
        viewModel.restoreZoomForPair(pair.zoomLevel)
        cameraSession.setZoom(viewModel.zoomUiState.value.currentRatio)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AfterCameraEvent.AfterSaved -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                is AfterCameraEvent.AllCompleted -> {
                    snackbarController.show(
                        SnackbarEvent(
                            UiText.Resource(CoreR.string.snackbar_success_all_after_captured),
                            SnackbarVariant.SUCCESS,
                        ),
                    )
                    delay(ALL_COMPLETED_NAVIGATE_BACK_DELAY_MS)
                    onNavigateBack()
                }

                is AfterCameraEvent.CaptureError -> {
                    snackbarController.show(
                        SnackbarEvent(
                            UiText.Resource(CoreR.string.snackbar_error_capture_failed),
                            SnackbarVariant.ERROR,
                        ),
                    )
                }

                is AfterCameraEvent.SaveError -> {
                    snackbarController.show(
                        SnackbarEvent(
                            UiText.Resource(CoreR.string.snackbar_error_unknown),
                            SnackbarVariant.ERROR,
                        ),
                    )
                }
            }
        }
    }

    val density = LocalDensity.current
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(PairShotCameraTokens.Letterbox)) {
        val safeTopPx = WindowInsets.safeDrawing.getTop(density)
        val safeBottomPx = WindowInsets.safeDrawing.getBottom(density)
        val fullHeightPx = with(density) { maxHeight.roundToPx() }
        val safeAvailableHeightPx = (fullHeightPx - safeTopPx - safeBottomPx).coerceAtLeast(0)
        val safeAvailableHeightDp = with(density) { safeAvailableHeightPx.toDp() }
        val stripSectionHeight = BeforeStripHeight
        val shutterSectionHeight = CameraShutterHeight
        val bottomSpacerDesired = 32.dp
        val minPreviewHeight = 180.dp

        val reservedHeight = stripSectionHeight + shutterSectionHeight + bottomSpacerDesired
        val previewHeightRaw = safeAvailableHeightDp - reservedHeight
        val previewSectionHeight = if (previewHeightRaw >= minPreviewHeight) previewHeightRaw else minPreviewHeight
        val bottomSpacerHeight =
            if (previewHeightRaw >= minPreviewHeight) {
                bottomSpacerDesired
            } else {
                (safeAvailableHeightDp - (stripSectionHeight + shutterSectionHeight + previewSectionHeight))
                    .coerceAtLeast(0.dp)
            }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                PairShotBannerAd()
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
                    onZoomRatioChanged = { newRatio ->
                        viewModel.updateZoomRatio(newRatio)
                        cameraSession.setZoom(newRatio)
                    },
                    onPresetTapped = { preset ->
                        viewModel.onPresetTapped(preset)
                        cameraSession.setZoom(viewModel.zoomUiState.value.currentRatio)
                    },
                    onDragEnd = { viewModel.applyCustomRatio() },
                    onExposureReset = {
                        viewModel.setExposureIndex(0)
                        cameraSession.setExposureIndex(0)
                    },
                    onExposureAdjust = { index ->
                        viewModel.setExposureIndex(index)
                        cameraSession.setExposureIndex(index)
                    },
                    onTapToFocus = { x, y, w, h ->
                        cameraSession.startFocusAndMetering(x, y, w.toFloat(), h.toFloat())
                    },
                    onToggleLens = {
                        val next = viewModel.toggleLensFacing()
                        cameraSession.setLensFacing(next)
                        cameraSession.setZoom(viewModel.zoomUiState.value.currentRatio)
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
                    emptyMessage = stringResource(R.string.camera_strip_empty_after),
                    stripHeight = stripSectionHeight,
                    progress = StripProgress(completed = completedCount, total = totalPairCount),
                )

                CameraBottomBar(
                    isSaving = isSaving,
                    shutterEnabled = currentPair != null,
                    height = shutterSectionHeight,
                    onToggleSettings = { viewModel.toggleSettingsPanel() },
                    onShutterClick = {
                        if (isSaving) return@CameraBottomBar
                        val pair = currentPair ?: return@CameraBottomBar
                        showBlackout = true
                        scope.launch {
                            viewModel.startCapturing()
                            val captureResult = cameraSession.capture()
                            val tempUri = captureResult.getOrNull()
                            if (captureResult.isFailure || tempUri == null) {
                                viewModel.emitCaptureError(
                                    captureResult.exceptionOrNull()?.message ?: "capture failed",
                                )
                                viewModel.finishCapturing()
                                return@launch
                            }
                            viewModel.saveAfterPhoto(tempUri)
                        }
                    },
                    lastPairThumbnailUri = lastPairThumbnailUri,
                    onThumbnailClick = onNavigateBack,
                )

                Spacer(modifier = Modifier.height(bottomSpacerHeight))
            }

            CameraSettingsSheet(
                visible = settingsState.showPanel,
                settingsState = settingsState,
                capabilities = capabilities,
                onToggleGrid = viewModel::toggleGrid,
                onCycleFlash = {
                    val next = viewModel.cycleFlash()
                    cameraSession.setFlash(next)
                },
                onToggleNightMode = {
                    val next = viewModel.toggleNightMode()
                    cameraSession.setNightMode(next)
                    if (next) cameraSession.setHdrMode(false)
                },
                onToggleHdr = {
                    val next = viewModel.toggleHdr()
                    cameraSession.setHdrMode(next)
                    if (next) cameraSession.setNightMode(false)
                },
                onToggleLevel = viewModel::toggleLevel,
                onDismiss = viewModel::dismissSettingsPanel,
                overlayEnabled = overlayEnabled,
                onToggleOverlay = viewModel::toggleOverlay,
                overlayAlpha = overlayAlpha,
                onOverlayAlphaChange = viewModel::updateOverlayAlpha,
                sortOrder = sortOrder,
                onToggleSortOrder = viewModel::toggleSortOrder,
            )

            PairShotSnackbarHost(
                controller = snackbarController,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = PairShotSpacing.snackbarTopOffset),
            )
        }
    }
}
