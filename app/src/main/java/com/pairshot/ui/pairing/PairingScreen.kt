package com.pairshot.ui.pairing

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
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
import coil3.compose.AsyncImage
import com.pairshot.ui.component.OverlayGuide
import com.pairshot.ui.component.ShutterButton
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.roundToInt

@Composable
fun PairingScreen(
    projectId: Long,
    initialPairId: Long? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: PairingViewModel = hiltViewModel(),
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
                PairingContent(
                    viewModel = viewModel,
                    onNavigateBack = onNavigateBack,
                )
            }

            permissionPermanentlyDenied -> {
                PairingPermissionDeniedContent(
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
                PairingPermissionRationaleContent(
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
private fun PairingContent(
    viewModel: PairingViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    val unpairedPhotos by viewModel.unpairedPhotos.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    val lensFacing by viewModel.lensFacing.collectAsStateWithLifecycle()
    val zoomRatio by viewModel.zoomRatio.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val overlayAlpha by viewModel.overlayAlpha.collectAsStateWithLifecycle()

    val currentPair = unpairedPhotos.getOrNull(currentIndex)
    val totalCount = unpairedPhotos.size

    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

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
            val path = candidates.firstOrNull { File(it).exists() }
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

    // unpairedPhotos 갱신 시 인덱스 보정 및 완료 체크
    LaunchedEffect(unpairedPhotos) {
        viewModel.onUnpairedPhotosUpdated(unpairedPhotos)
        if (unpairedPhotos.isEmpty()) {
            viewModel.emitAllCompleted()
        }
    }

    // 현재 인덱스 변경 시 썸네일 스트립 자동 스크롤
    LaunchedEffect(currentIndex) {
        if (unpairedPhotos.isNotEmpty()) {
            thumbnailListState.animateScrollToItem(currentIndex)
        }
    }

    // 현재 pair 변경 시 해당 Before의 zoomLevel을 카메라에 적용
    LaunchedEffect(currentIndex, unpairedPhotos) {
        val pair = unpairedPhotos.getOrNull(currentIndex) ?: return@LaunchedEffect
        val zoom = pair.zoomLevel ?: 1f
        cameraControl?.setZoomRatio(zoom)
        viewModel.updateZoomRatio(zoom)
    }

    // 이벤트 수신
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PairingEvent.AfterSaved -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                is PairingEvent.AllCompleted -> {
                    snackbarHostState.showSnackbar("모든 After 촬영이 완료되었습니다!")
                    delay(2000L)
                    onNavigateBack()
                }

                is PairingEvent.CaptureError -> {
                    snackbarHostState.showSnackbar("촬영에 실패했습니다. 다시 시도해주세요.")
                }

                is PairingEvent.SaveError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // CameraX 바인딩
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

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

        // 카메라 전환 시 현재 pair의 zoom 비율 적용
        val zoom = unpairedPhotos.getOrNull(currentIndex)?.zoomLevel ?: 1f
        camera.cameraControl.setZoomRatio(zoom)
        viewModel.updateZoomRatio(zoom)
    }

    // 화면 이탈 시 카메라 리소스 해제
    DisposableEffect(Unit) {
        onDispose { cameraProvider?.unbindAll() }
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
        // 1. CameraX Preview
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

        // 2. Before 오버레이
        OverlayGuide(
            imageUri = currentPair?.beforePhotoUri,
            alpha = overlayAlpha,
            modifier = Modifier.fillMaxSize(),
        )

        // 3. 줌 배율 표시 — 프리뷰 하단 중앙 (컨트롤 바 위)
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
                    .padding(bottom = 148.dp),
        )

        // 4. 상단 반투명 바: 뒤로가기 + 카운터
        Row(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White,
                )
            }
            Text(
                text = if (totalCount > 0) "${currentIndex + 1} / $totalCount (미완료)" else "미완료 없음",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }

        // 5. 하단 컨트롤 바
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.1f))
                    .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(top = 12.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 셔터 + 카메라 전환 + 설정 Row
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
            ) {
                // 가운데: 셔터 버튼
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
                        val tempFile = File(tempDir, "after_${System.currentTimeMillis()}.jpg")
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
                                    viewModel.onAfterCaptured(savedUri.toString())
                                }
                            },
                        )
                    },
                    enabled = !isSaving && currentPair != null,
                    modifier = Modifier.align(Alignment.Center),
                )

                // 오른쪽: 카메라 전환 + 설정
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { viewModel.toggleLensFacing() }) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "카메라 전환",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    IconButton(onClick = { /* 설정 — 추후 구현 */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Before 썸네일 스트립: ◀ [썸네일 고정 5개] ▶
            val maxVisibleThumbnails = 5
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // 이전 화살표
                IconButton(
                    onClick = { viewModel.moveToPrevious() },
                    enabled = currentIndex > 0,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = "이전",
                        tint = if (currentIndex > 0) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp),
                    )
                }

                // 고정 너비 썸네일 영역 (최대 5개 × 40dp + 간격)
                val thumbnailSize = 40.dp
                val thumbnailGap = 6.dp
                val stripWidth = thumbnailSize * maxVisibleThumbnails + thumbnailGap * (maxVisibleThumbnails - 1)

                LazyRow(
                    state = thumbnailListState,
                    horizontalArrangement = Arrangement.spacedBy(thumbnailGap),
                    modifier = Modifier.width(stripWidth),
                    contentPadding = PaddingValues(horizontal = 0.dp),
                ) {
                    itemsIndexed(
                        items = unpairedPhotos,
                        key = { _, pair -> pair.id },
                    ) { index, pair ->
                        val isSelected = index == currentIndex
                        AsyncImage(
                            model = pair.beforePhotoUri,
                            contentDescription = "Before 썸네일 ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier =
                                Modifier
                                    .size(thumbnailSize)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp),
                                    ).clickable { viewModel.selectIndex(index) },
                        )
                    }
                }

                // 다음 화살표
                IconButton(
                    onClick = { viewModel.moveToNext() },
                    enabled = currentIndex < totalCount - 1,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = "다음",
                        tint = if (currentIndex < totalCount - 1) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }

        // 6. Snackbar — 화면 중앙, 문구에 맞는 너비
        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.Center),
            snackbar = { snackbarData ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 6.dp,
                    ) {
                        Text(
                            text = snackbarData.visuals.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun PairingPermissionRationaleContent(
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
            text = "After 사진 촬영을 위해 카메라 접근 권한이 필요합니다.",
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
private fun PairingPermissionDeniedContent(
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
