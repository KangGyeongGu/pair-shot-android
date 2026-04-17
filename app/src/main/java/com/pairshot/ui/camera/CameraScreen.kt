package com.pairshot.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.ui.component.BeforePreviewStrip
import com.pairshot.ui.component.ShutterButton
import java.io.File
import kotlin.math.roundToInt

@Composable
fun CameraScreen(
    projectId: Long,
    onNavigateBack: () -> Unit = {},
    viewModel: CameraViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED,
        )
    }
    var showRationale by remember { mutableStateOf(false) }
    var permissionPermanentlyDenied by remember { mutableStateOf(false) }
    val beforePreviewUris by viewModel.beforePreviewUris.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        viewModel.observeProject(projectId)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                hasCameraPermission = true
            } else {
                val shouldShowRationale =
                    activity?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
                    } ?: false
                if (shouldShowRationale) {
                    showRationale = true
                } else {
                    permissionPermanentlyDenied = true
                }
            }
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            val shouldShowRationale =
                activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
                } ?: false
            if (shouldShowRationale) {
                showRationale = true
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        when {
            hasCameraPermission -> {
                CameraPreviewContent(
                    projectId = projectId,
                    viewModel = viewModel,
                    beforePreviewUris = beforePreviewUris,
                    onNavigateBack = onNavigateBack,
                )
            }

            permissionPermanentlyDenied -> {
                PermissionDeniedContent(
                    onOpenSettings = {
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        context.startActivity(intent)
                    },
                    onNavigateBack = onNavigateBack,
                )
            }

            showRationale -> {
                PermissionRationaleContent(
                    onRequestPermission = {
                        showRationale = false
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onNavigateBack = onNavigateBack,
                )
            }

            else -> {
                // 권한 요청 중 — 빈 검은 화면 유지
            }
        }
    }
}

@Composable
private fun CameraPreviewContent(
    projectId: Long,
    viewModel: CameraViewModel,
    beforePreviewUris: List<String>,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    val lensFacing by viewModel.lensFacing.collectAsStateWithLifecycle()
    val zoomRatio by viewModel.zoomRatio.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

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

    LaunchedEffect(lensFacing) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        cameraProvider = provider
        provider.unbindAll()

        val cameraSelector =
            CameraSelector
                .Builder()
                .requireLensFacing(lensFacing)
                .build()

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider { request ->
            surfaceRequest = request
        }

        val camera =
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                viewModel.imageCapture,
            )
        cameraControl = camera.cameraControl

        // 카메라 전환 시 줌 비율 초기화
        camera.cameraControl.setZoomRatio(1f)
        viewModel.updateZoomRatio(1f)
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
                            .pointerInput(cameraControl) {
                                detectTransformGestures { _, _, zoom, _ ->
                                    val control = cameraControl ?: return@detectTransformGestures
                                    val newRatio = (zoomRatio * zoom).coerceIn(1f, 10f)
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

                    val zoomText =
                        if (zoomRatio == 1f) {
                            "1.0x"
                        } else {
                            "${(zoomRatio * 10).roundToInt() / 10f}x"
                        }
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
                            Text(
                                text = zoomText,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp)
                                        .background(
                                            color = Color.Black.copy(alpha = 0.45f),
                                            shape = RoundedCornerShape(10.dp),
                                        ).padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
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
                        IconButton(onClick = { /* 설정 — 추후 구현 */ }) {
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
private fun PermissionRationaleContent(
    onRequestPermission: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "카메라 권한 필요",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "PairShot은 사진 촬영을 위해 카메라 접근 권한이 필요합니다.",
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(32.dp))
        androidx.compose.material3.Button(onClick = onRequestPermission) {
            Text(text = "권한 허용")
        }
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.material3.TextButton(onClick = onNavigateBack) {
            Text(text = "취소", color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onOpenSettings: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "카메라 권한이 거부되었습니다",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "설정 앱에서 PairShot의 카메라 권한을 직접 허용해 주세요.",
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(32.dp))
        androidx.compose.material3.Button(onClick = onOpenSettings) {
            Text(text = "설정으로 이동")
        }
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.material3.TextButton(onClick = onNavigateBack) {
            Text(text = "취소", color = Color.White.copy(alpha = 0.6f))
        }
    }
}
