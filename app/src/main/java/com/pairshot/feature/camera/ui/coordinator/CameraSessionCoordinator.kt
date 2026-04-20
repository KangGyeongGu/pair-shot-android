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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.concurrent.futures.await
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pairshot.feature.camera.ui.state.FlashMode

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

    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        cameraProviderState.value = provider
        extensionsManagerState.value = ExtensionsManager.getInstanceAsync(context, provider).await()
    }

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

        camera.cameraInfo.zoomState.removeObservers(lifecycleOwner)
        camera.cameraInfo.zoomState.observe(lifecycleOwner) { zoomState ->
            if (zoomState != null) {
                onZoomStateReady(zoomState.minZoomRatio, zoomState.maxZoomRatio)
            }
        }

        onInitialZoom(camera.cameraControl)
    }

    LaunchedEffect(flashMode) {
        val control = cameraControlProvider() ?: return@LaunchedEffect
        applyFlashMode(imageCapture)
        control.enableTorch(flashMode == FlashMode.TORCH)
    }

    LaunchedEffect(exposureIndex) {
        cameraControlProvider()?.setExposureCompensationIndex(exposureIndex)
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    cameraProviderState.value?.unbindAll()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
