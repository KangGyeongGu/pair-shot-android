package com.pairshot.feature.camera.ui.screen

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
import com.pairshot.feature.camera.ui.chrome.BeforeCameraTopBar
import com.pairshot.feature.camera.ui.chrome.CameraBottomBar
import com.pairshot.feature.camera.ui.component.BeforePreviewStrip
import com.pairshot.feature.camera.ui.component.CameraSettingsSheet
import com.pairshot.feature.camera.ui.preview.CameraPreviewPane
import com.pairshot.feature.camera.ui.viewmodel.CameraEvent
import com.pairshot.feature.camera.ui.viewmodel.CameraViewModel

private val CameraTopBarHeight = 56.dp
private val CameraStripHeight = 120.dp
private val CameraShutterHeight = 116.dp

@Composable
internal fun CameraScreen(
    projectId: Long,
    viewModel: CameraViewModel,
    onNavigateBack: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val beforePreviewUris by viewModel.beforePreviewUris.collectAsStateWithLifecycle()
    val zoomUiState by viewModel.zoomUiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilities.collectAsStateWithLifecycle()
    val roll by viewModel.roll.collectAsStateWithLifecycle()
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()

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

    LaunchedEffect(projectId) { viewModel.observeProject(projectId) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CameraEvent.PhotoSaved -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                is CameraEvent.CaptureError -> {
                    snackbarController.show(
                        SnackbarEvent("촬영에 실패했습니다. 다시 시도해주세요.", SnackbarVariant.ERROR),
                    )
                }

                is CameraEvent.SaveError -> {
                    snackbarController.show(
                        SnackbarEvent("오류", SnackbarVariant.ERROR),
                    )
                }
            }
        }
    }

    LaunchedEffect(beforePreviewUris.size) {
        if (beforePreviewUris.isNotEmpty()) {
            thumbnailListState.animateScrollToItem(beforePreviewUris.lastIndex)
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
                BeforeCameraTopBar(
                    onNavigateBack = onNavigateBack,
                    height = topSectionHeight,
                )

                CameraPreviewPane(
                    surfaceRequest = surfaceRequest,
                    zoomUiState = zoomUiState,
                    blackoutAlpha = blackoutAlpha,
                    gridEnabled = settingsState.gridEnabled,
                    levelEnabled = settingsState.levelEnabled,
                    roll = roll,
                    exposureRange = capabilities.exposureRange,
                    currentExposureIndex = settingsState.exposureIndex,
                    exposureStep = capabilities.exposureStep,
                    height = previewSectionHeight,
                    onZoomRatioChanged = { newRatio -> viewModel.updateZoomRatio(newRatio) },
                    onPresetTapped = { preset -> viewModel.onPresetTapped(preset) },
                    onDragEnd = { viewModel.applyCustomRatio() },
                    onExposureReset = { viewModel.setExposureIndex(0) },
                    onExposureAdjust = { index -> viewModel.setExposureIndex(index) },
                    onTapToFocus = { x, y, w, h ->
                        viewModel.onFocusRequested(x, y, w.toFloat(), h.toFloat())
                    },
                )

                BeforePreviewStrip(
                    beforePreviewUris = beforePreviewUris,
                    modifier = Modifier.height(stripSectionHeight),
                    listState = thumbnailListState,
                    stripHeight = stripSectionHeight,
                )

                CameraBottomBar(
                    isSaving = isSaving,
                    shutterEnabled = true,
                    height = shutterSectionHeight,
                    onToggleLens = { viewModel.toggleLensFacing() },
                    onToggleSettings = { viewModel.toggleSettingsPanel() },
                    onShutterClick = {
                        showBlackout = true
                        viewModel.onShutterClick(projectId)
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
