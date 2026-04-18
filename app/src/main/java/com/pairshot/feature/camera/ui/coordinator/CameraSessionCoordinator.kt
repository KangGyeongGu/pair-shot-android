package com.pairshot.feature.camera.ui.coordinator

import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.concurrent.futures.await
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pairshot.feature.camera.ui.state.FlashMode

/**
 * 카메라 바인딩 사이드이펙트 코디네이터.
 *
 * UI를 방출하지 않으며 LaunchedEffect 세 개만 실행한다:
 *  1. ExtensionsManager 초기화 (Unit key — 1회)
 *  2. 렌즈/Extension 전환 시 재바인딩 (lensFacing, nightModeEnabled, hdrEnabled 키)
 *  3. 플래시 모드 변경 반영 (flashMode 키)
 *  4. 노출 보정 변경 반영 (exposureIndex 키)
 *
 * @param cameraControlProvider 현재 활성 CameraControl 을 반환하는 람다.
 *   flash/exposure LaunchedEffect 에서 호출되며, 바인딩 전이면 null 반환.
 */
@Composable
internal fun CameraSessionCoordinator(
    lensFacing: Int,
    nightModeEnabled: Boolean,
    hdrEnabled: Boolean,
    flashMode: FlashMode,
    exposureIndex: Int,
    imageCapture: ImageCapture,
    cameraProviderState: MutableState<ProcessCameraProvider?>,
    extensionsManagerState: MutableState<ExtensionsManager?>,
    getExtensionCameraSelector: (ExtensionsManager) -> CameraSelector,
    applyFlashMode: (ImageCapture) -> Unit,
    cameraControlProvider: () -> CameraControl?,
    onSurfaceRequest: (SurfaceRequest) -> Unit,
    onCameraReady: (CameraControl) -> Unit,
    onZoomStateReady: (minRatio: Float, maxRatio: Float) -> Unit,
    onCapabilitiesReady: (CameraInfo, ExtensionsManager) -> Unit,
    onInitialZoom: suspend (CameraControl) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ExtensionsManager 초기화 — ProcessCameraProvider당 1회만 수행
    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        cameraProviderState.value = provider
        extensionsManagerState.value = ExtensionsManager.getInstanceAsync(context, provider).await()
    }

    // 렌즈 전환 / Extension 토글 시 카메라 재바인딩 — 단일 LaunchedEffect로 race condition 방지
    LaunchedEffect(lensFacing, nightModeEnabled, hdrEnabled) {
        val provider =
            cameraProviderState.value
                ?: ProcessCameraProvider
                    .awaitInstance(context)
                    .also { cameraProviderState.value = it }
        val extManager =
            extensionsManagerState.value
                ?: ExtensionsManager
                    .getInstanceAsync(context, provider)
                    .await()
                    .also { extensionsManagerState.value = it }
        provider.unbindAll()

        val cameraSelector = getExtensionCameraSelector(extManager)

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider { request -> onSurfaceRequest(request) }

        applyFlashMode(imageCapture)

        val camera =
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
            )
        onCameraReady(camera.cameraControl)
        onCapabilitiesReady(camera.cameraInfo, extManager)

        if (flashMode == FlashMode.TORCH) {
            camera.cameraControl.enableTorch(true)
        }

        if (exposureIndex != 0) {
            camera.cameraControl.setExposureCompensationIndex(exposureIndex)
        }

        // 기존 observer 제거 후 재등록 — observer 누적 방지
        camera.cameraInfo.zoomState.removeObservers(lifecycleOwner)
        camera.cameraInfo.zoomState.observe(lifecycleOwner) { zoomState ->
            if (zoomState != null) {
                onZoomStateReady(zoomState.minZoomRatio, zoomState.maxZoomRatio)
            }
        }

        onInitialZoom(camera.cameraControl)
    }

    // 플래시 모드 변경 반영
    LaunchedEffect(flashMode) {
        val control = cameraControlProvider() ?: return@LaunchedEffect
        applyFlashMode(imageCapture)
        control.enableTorch(flashMode == FlashMode.TORCH)
    }

    // 노출 보정 변경 반영
    LaunchedEffect(exposureIndex) {
        cameraControlProvider()?.setExposureCompensationIndex(exposureIndex)
    }
}
