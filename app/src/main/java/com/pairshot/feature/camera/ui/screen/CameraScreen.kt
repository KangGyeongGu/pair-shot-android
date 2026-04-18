package com.pairshot.feature.camera.ui.screen

import android.media.AudioManager
import android.media.MediaPlayer
import androidx.camera.core.CameraControl
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.feature.camera.ui.chrome.BeforeCameraTopBar
import com.pairshot.feature.camera.ui.chrome.CameraBottomBar
import com.pairshot.feature.camera.ui.component.BeforePreviewStrip
import com.pairshot.feature.camera.ui.component.CameraSettingsSheet
import com.pairshot.feature.camera.ui.coordinator.CameraSessionCoordinator
import com.pairshot.feature.camera.ui.preview.CameraPreviewPane
import com.pairshot.feature.camera.ui.viewmodel.CameraEvent
import com.pairshot.feature.camera.ui.viewmodel.CameraViewModel
import kotlin.math.roundToInt

@Composable
internal fun CameraScreen(
    projectId: Long,
    viewModel: CameraViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val beforePreviewUris by viewModel.beforePreviewUris.collectAsStateWithLifecycle()
    val lensFacing by viewModel.lensFacing.collectAsStateWithLifecycle()
    val zoomUiState by viewModel.zoomUiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilities.collectAsStateWithLifecycle()
    val roll by viewModel.levelSensorManager.roll.collectAsStateWithLifecycle()

    var surfaceRequest by remember { mutableStateOf<androidx.camera.core.SurfaceRequest?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    val cameraProviderState = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val extensionsManagerState = remember { mutableStateOf<ExtensionsManager?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val thumbnailListState = rememberLazyListState()

    val audioManager =
        remember {
            context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
        }
    val shutterPlayer =
        remember {
            val candidates =
                listOf(
                    "/system/media/audio/ui/camera_click.ogg",
                    "/system/media/audio/ui/camera_shutter.ogg",
                    "/system/media/audio/ui/CameraClick.ogg",
                )
            val path = candidates.firstOrNull { java.io.File(it).exists() }
            path?.let {
                MediaPlayer().apply {
                    setDataSource(it)
                    prepare()
                }
            }
        }
    DisposableEffect(Unit) {
        onDispose {
            shutterPlayer?.release()
            cameraProviderState.value?.unbindAll()
            cameraProviderState.value = null
            surfaceRequest = null
            cameraControl = null
        }
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
                is CameraEvent.PhotoSaved -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                is CameraEvent.CaptureError -> snackbarHostState.showSnackbar("촬영에 실패했습니다. 다시 시도해주세요.")
                is CameraEvent.SaveError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(beforePreviewUris.size) {
        if (beforePreviewUris.isNotEmpty()) {
            thumbnailListState.animateScrollToItem(beforePreviewUris.lastIndex)
        }
    }

    CameraSessionCoordinator(
        lensFacing = lensFacing,
        nightModeEnabled = settingsState.nightModeEnabled,
        hdrEnabled = settingsState.hdrEnabled,
        flashMode = settingsState.flashMode,
        exposureIndex = settingsState.exposureIndex,
        imageCapture = viewModel.imageCapture,
        cameraProviderState = cameraProviderState,
        extensionsManagerState = extensionsManagerState,
        getExtensionCameraSelector = { extManager -> viewModel.getExtensionCameraSelector(extManager) },
        applyFlashMode = { ic -> viewModel.applyFlashMode(ic) },
        cameraControlProvider = { cameraControl },
        onSurfaceRequest = { surfaceRequest = it },
        onCameraReady = { cameraControl = it },
        onZoomStateReady = { min, max -> viewModel.initFromZoomState(min, max) },
        onCapabilitiesReady = { info, extManager -> viewModel.updateCapabilities(info, extManager) },
        onInitialZoom = { control ->
            viewModel.resetZoomForLensSwitch()
            control.setZoomRatio(viewModel.zoomUiState.value.currentRatio)
        },
    )

    val density = LocalDensity.current
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val safeTopPx = WindowInsets.safeDrawing.getTop(density)
        val safeBottomPx = WindowInsets.safeDrawing.getBottom(density)
        val fullHeightPx = with(density) { maxHeight.roundToPx() }
        val safeAvailableHeightPx = (fullHeightPx - safeTopPx - safeBottomPx).coerceAtLeast(0)
        val safeAvailableHeightDp = with(density) { safeAvailableHeightPx.toDp() }
        val topSectionHeight = 56.dp
        val stripSectionHeight = 120.dp
        val shutterSectionHeight = 116.dp
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
                    cameraControl = cameraControl,
                    zoomUiState = zoomUiState,
                    blackoutAlpha = blackoutAlpha,
                    gridEnabled = settingsState.gridEnabled,
                    levelEnabled = settingsState.levelEnabled,
                    roll = roll,
                    exposureRange = capabilities.exposureRange,
                    currentExposureIndex = settingsState.exposureIndex,
                    exposureStep = capabilities.exposureStep,
                    height = previewSectionHeight,
                    onZoomRatioChanged = { newRatio ->
                        cameraControl?.setZoomRatio(newRatio)
                        viewModel.updateZoomRatio(newRatio)
                    },
                    onPresetTapped = { preset ->
                        viewModel.onPresetTapped(preset)
                        cameraControl?.setZoomRatio(viewModel.zoomUiState.value.currentRatio)
                    },
                    onDragEnd = { viewModel.applyCustomRatio() },
                    onExposureReset = {
                        viewModel.setExposureIndex(0)
                        cameraControl?.setExposureCompensationIndex(0)
                    },
                    onExposureAdjust = { index ->
                        viewModel.setExposureIndex(index)
                        cameraControl?.setExposureCompensationIndex(index)
                    },
                )

                BeforePreviewStrip(
                    beforePreviewUris = beforePreviewUris,
                    modifier = Modifier.height(stripSectionHeight),
                    listState = thumbnailListState,
                    stripHeight = stripSectionHeight,
                )

                CameraBottomBar(
                    imageCapture = viewModel.imageCapture,
                    isSaving = isSaving,
                    shutterEnabled = true,
                    shutterPlayer = shutterPlayer,
                    audioManager = audioManager,
                    tempFilePrefix = "capture_",
                    height = shutterSectionHeight,
                    onToggleLens = { viewModel.toggleLensFacing() },
                    onToggleSettings = { viewModel.toggleSettingsPanel() },
                    onShowBlackout = { showBlackout = true },
                    onImageSaved = { uri ->
                        viewModel.onShutterClick(projectId = projectId, tempFileUri = uri)
                    },
                    onCaptureError = { msg -> viewModel.emitCaptureError(msg) },
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
                hostState = snackbarHostState,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 112.dp),
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(16.dp),
                    )
                },
            )
        }
    }
}
