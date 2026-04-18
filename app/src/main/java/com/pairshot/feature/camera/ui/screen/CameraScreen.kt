package com.pairshot.feature.camera.ui.screen

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.feature.camera.ui.component.BeforePreviewStrip
import com.pairshot.feature.camera.ui.component.CameraSettingsSheet
import com.pairshot.feature.camera.ui.component.FocusExposureOverlay
import com.pairshot.feature.camera.ui.component.GridOverlay
import com.pairshot.feature.camera.ui.component.LevelOverlay
import com.pairshot.feature.camera.ui.component.ShutterButton
import com.pairshot.feature.camera.ui.component.ZoomControls
import com.pairshot.feature.camera.ui.state.FlashMode
import com.pairshot.feature.camera.ui.viewmodel.CameraEvent
import com.pairshot.feature.camera.ui.viewmodel.CameraViewModel
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@Composable
internal fun CameraScreen(
    projectId: Long,
    viewModel: CameraViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    val beforePreviewUris by viewModel.beforePreviewUris.collectAsStateWithLifecycle()
    val lensFacing by viewModel.lensFacing.collectAsStateWithLifecycle()
    val zoomUiState by viewModel.zoomUiState.collectAsStateWithLifecycle()
    val latestZoomRatio by rememberUpdatedState(zoomUiState.currentRatio)
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilities.collectAsStateWithLifecycle()
    val roll by viewModel.levelSensorManager.roll.collectAsStateWithLifecycle()

    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var extensionsManager by remember { mutableStateOf<ExtensionsManager?>(null) }

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
            cameraProvider?.unbindAll()
            cameraProvider = null
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

    LaunchedEffect(projectId) {
        viewModel.observeProject(projectId)
    }

    // CameraEvent 수신 — 촬영 성공 시 햅틱, 실패 시 Snackbar
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CameraEvent.PhotoSaved -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                is CameraEvent.CaptureError -> {
                    snackbarHostState.showSnackbar("촬영에 실패했습니다. 다시 시도해주세요.")
                }

                is CameraEvent.SaveError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // ExtensionsManager 초기화 — ProcessCameraProvider당 1회만 수행
    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        cameraProvider = provider
        extensionsManager = ExtensionsManager.getInstanceAsync(context, provider).await()
    }

    // 렌즈 전환 / Extension 토글 시 카메라 재바인딩 — 단일 LaunchedEffect로 race condition 방지
    LaunchedEffect(lensFacing, settingsState.nightModeEnabled, settingsState.hdrEnabled) {
        val provider = cameraProvider ?: ProcessCameraProvider.awaitInstance(context).also { cameraProvider = it }
        val extManager = extensionsManager ?: ExtensionsManager.getInstanceAsync(context, provider).await().also { extensionsManager = it }
        provider.unbindAll()

        val cameraSelector = viewModel.getExtensionCameraSelector(extManager)

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider { request ->
            surfaceRequest = request
        }

        viewModel.applyFlashMode(viewModel.imageCapture)

        val camera =
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                viewModel.imageCapture,
            )
        cameraControl = camera.cameraControl

        viewModel.updateCapabilities(camera.cameraInfo, extManager)

        if (viewModel.settingsState.value.flashMode == FlashMode.TORCH) {
            camera.cameraControl.enableTorch(true)
        }

        // 노출 보정 복원
        if (viewModel.settingsState.value.exposureIndex != 0) {
            camera.cameraControl.setExposureCompensationIndex(viewModel.settingsState.value.exposureIndex)
        }

        // 기존 observer 제거 후 재등록 — observer 누적 방지
        camera.cameraInfo.zoomState.removeObservers(lifecycleOwner)
        camera.cameraInfo.zoomState.observe(lifecycleOwner) { zoomState ->
            if (zoomState != null) {
                viewModel.initFromZoomState(zoomState.minZoomRatio, zoomState.maxZoomRatio)
            }
        }

        viewModel.resetZoomForLensSwitch()
        val resetRatio = viewModel.zoomUiState.value.currentRatio
        camera.cameraControl.setZoomRatio(resetRatio)
    }

    // 플래시 모드 변경 반영
    LaunchedEffect(settingsState.flashMode) {
        val control = cameraControl ?: return@LaunchedEffect
        viewModel.applyFlashMode(viewModel.imageCapture)
        // Torch 모드 on/off
        control.enableTorch(settingsState.flashMode == FlashMode.TORCH)
    }

    // 노출 보정 변경 반영
    LaunchedEffect(settingsState.exposureIndex) {
        cameraControl?.setExposureCompensationIndex(settingsState.exposureIndex)
    }

    LaunchedEffect(beforePreviewUris.size) {
        if (beforePreviewUris.isNotEmpty()) {
            thumbnailListState.animateScrollToItem(beforePreviewUris.lastIndex)
        }
    }

    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
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

        val reservedHeight =
            topSectionHeight + stripSectionHeight + shutterSectionHeight + bottomSpacerDesired
        val previewHeightRaw = safeAvailableHeightDp - reservedHeight
        val previewSectionHeight =
            if (previewHeightRaw >= minPreviewHeight) {
                previewHeightRaw
            } else {
                minPreviewHeight
            }
        val bottomSpacerHeight =
            if (previewHeightRaw >= minPreviewHeight) {
                bottomSpacerDesired
            } else {
                (
                    safeAvailableHeightDp -
                        (topSectionHeight + stripSectionHeight + shutterSectionHeight + previewSectionHeight)
                ).coerceAtLeast(0.dp)
            }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(topSectionHeight)
                            .background(Color.Black),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "뒤로가기",
                                tint = Color.White,
                            )
                        }
                    }
                }

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(previewSectionHeight)
                            .pointerInput(cameraControl, zoomUiState.minRatio, zoomUiState.maxRatio) {
                                detectTransformGestures { _, _, zoom, _ ->
                                    val control = cameraControl ?: return@detectTransformGestures
                                    val newRatio =
                                        (latestZoomRatio * zoom)
                                            .coerceIn(zoomUiState.minRatio, zoomUiState.maxRatio)
                                    control.setZoomRatio(newRatio)
                                    viewModel.updateZoomRatio(newRatio)
                                }
                            },
                ) {
                    surfaceRequest?.let { request ->
                        Box {
                            CameraXViewfinder(
                                surfaceRequest = request,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize(),
                            )
                            if (blackoutAlpha > 0f) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = blackoutAlpha)),
                                )
                            }
                        }
                    }

                    // 탭-투-포커스 + 드래그 노출 보정 오버레이
                    FocusExposureOverlay(
                        onTapToFocus = { x, y, viewWidth, viewHeight ->
                            val control = cameraControl ?: return@FocusExposureOverlay
                            val factory =
                                SurfaceOrientedMeteringPointFactory(
                                    viewWidth.toFloat(),
                                    viewHeight.toFloat(),
                                )
                            val point = factory.createPoint(x, y)
                            val action =
                                FocusMeteringAction
                                    .Builder(point)
                                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                                    .build()
                            control.startFocusAndMetering(action)
                        },
                        onExposureReset = {
                            viewModel.setExposureIndex(0)
                            cameraControl?.setExposureCompensationIndex(0)
                        },
                        onExposureAdjust = { index ->
                            viewModel.setExposureIndex(index)
                            cameraControl?.setExposureCompensationIndex(index)
                        },
                        exposureRange = capabilities.exposureRange,
                        currentExposureIndex = settingsState.exposureIndex,
                        exposureStep = capabilities.exposureStep,
                        modifier = Modifier.fillMaxSize(),
                    )

                    BoxWithConstraints(
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .fillMaxSize(),
                    ) {
                        val containerRatio =
                            if (maxHeight.value > 0f) {
                                maxWidth.value / maxHeight.value
                            } else {
                                3f / 4f
                            }
                        val requestedRatioRaw =
                            surfaceRequest?.resolution?.let { size ->
                                if (size.height > 0) {
                                    size.width.toFloat() / size.height.toFloat()
                                } else {
                                    containerRatio
                                }
                            } ?: containerRatio
                        val requestedRatio =
                            when {
                                requestedRatioRaw <= 0f -> containerRatio
                                (requestedRatioRaw > 1f) != (containerRatio > 1f) -> 1f / requestedRatioRaw
                                else -> requestedRatioRaw
                            }

                        val previewFrameModifier =
                            if (containerRatio > requestedRatio) {
                                Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(requestedRatio)
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(requestedRatio)
                            }

                        Box(modifier = previewFrameModifier.align(Alignment.Center)) {
                            // 배율 프리셋 버튼 + 수평 줌 다이얼
                            ZoomControls(
                                zoomUiState = zoomUiState,
                                onZoomRatioChanged = { newRatio ->
                                    cameraControl?.setZoomRatio(newRatio)
                                    viewModel.updateZoomRatio(newRatio)
                                },
                                onPresetTapped = { preset ->
                                    viewModel.onPresetTapped(preset)
                                    cameraControl?.setZoomRatio(
                                        viewModel.zoomUiState.value.currentRatio,
                                    )
                                },
                                onDragEnd = { viewModel.applyCustomRatio() },
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp),
                            )
                        }
                    }

                    // 격자선 + 수평계 오버레이
                    CameraPreviewOverlays(
                        gridEnabled = settingsState.gridEnabled,
                        levelEnabled = settingsState.levelEnabled,
                        roll = roll,
                    )
                }

                BeforePreviewStrip(
                    beforePreviewUris = beforePreviewUris,
                    modifier = Modifier.height(stripSectionHeight),
                    listState = thumbnailListState,
                    stripHeight = stripSectionHeight,
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(shutterSectionHeight)
                            .background(Color.Black)
                            .padding(horizontal = 32.dp),
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { viewModel.toggleLensFacing() }) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "카메라 전환",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        IconButton(onClick = { viewModel.toggleSettingsPanel() }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "설정",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }

                    ShutterButton(
                        onClick = {
                            shutterPlayer?.let { player ->
                                val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                val ratio = if (max > 0) current.toFloat() / max else 0f
                                val vol = ratio * 0.10f
                                player.setVolume(vol, vol)
                                if (player.isPlaying) player.seekTo(0) else player.start()
                            }
                            showBlackout = true

                            val tempDir = File(context.cacheDir, "temp").also { it.mkdirs() }
                            val tempFile = File(tempDir, "capture_${System.currentTimeMillis()}.jpg")
                            val outputFileOptions =
                                ImageCapture.OutputFileOptions
                                    .Builder(tempFile)
                                    .build()
                            viewModel.imageCapture.takePicture(
                                outputFileOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onError(exception: ImageCaptureException) {
                                        tempFile.delete()
                                        viewModel.emitCaptureError(exception.message ?: "촬영 실패")
                                    }

                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        val savedUri =
                                            outputFileResults.savedUri
                                                ?: Uri.fromFile(tempFile)
                                        viewModel.onShutterClick(
                                            projectId = projectId,
                                            tempFileUri = savedUri.toString(),
                                        )
                                    }
                                },
                            )
                        },
                        enabled = !isSaving,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

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

@Composable
private fun CameraPreviewOverlays(
    gridEnabled: Boolean,
    levelEnabled: Boolean,
    roll: Float,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = gridEnabled,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            GridOverlay()
        }

        AnimatedVisibility(
            visible = levelEnabled,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            LevelOverlay(roll = roll)
        }
    }
}
