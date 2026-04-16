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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlipCameraAndroid
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    val snackbarHostState = remember { SnackbarHostState() }

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
        onDispose { shutterPlayer?.release() }
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
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        cameraProvider.unbindAll()

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
            cameraProvider.bindToLifecycle(
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

    Box(
        modifier =
            Modifier
                .fillMaxSize()
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

        // 줌 비율 표시 — 프리뷰 하단 중앙
        val zoomText =
            if (zoomRatio == 1f) {
                "1.0x"
            } else {
                "${(zoomRatio * 10).roundToInt() / 10f}x"
            }
        Text(
            text = zoomText,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 104.dp),
        )

        // 뒤로가기 버튼 — 좌상단
        Box(
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.45f),
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White,
                )
            }
        }

        // 하단 컨트롤 바
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.1f),
                    ).background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    ).windowInsetsPadding(WindowInsets.systemBars)
                    .padding(vertical = 20.dp, horizontal = 32.dp),
        ) {
            // 오른쪽: 카메라 전환 + 설정
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

            // 가운데: 셔터 버튼
            com.pairshot.ui.component.ShutterButton(
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

        // Snackbar — 촬영 실패 시 표시 (architecture.md 6.5: surfaceVariant 배경 + medium 모서리, 하단에서 나타남)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 16.dp, vertical = 120.dp),
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
