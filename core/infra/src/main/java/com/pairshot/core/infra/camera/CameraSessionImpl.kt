package com.pairshot.core.infra.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.pairshot.core.infra.sensor.SensorSession
import com.pairshot.core.model.CameraCapabilities
import com.pairshot.core.model.FlashMode
import com.pairshot.core.model.LensFacing
import com.pairshot.core.model.ZoomRange
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@ViewModelScoped
class CameraSessionImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val sensorSession: SensorSession,
    ) : CameraSession {
        private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
        override val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

        private val _capabilities = MutableStateFlow(CameraCapabilities())
        override val capabilities: StateFlow<CameraCapabilities> = _capabilities.asStateFlow()

        private val _zoomState = MutableStateFlow(ZoomRange(1f, 1f, 1f))
        override val zoomState: StateFlow<ZoomRange> = _zoomState.asStateFlow()

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        private var provider: ProcessCameraProvider? = null
        private var extensionsManager: ExtensionsManager? = null
        private var camera: Camera? = null
        private var owner: LifecycleOwner? = null

        private val imageCapture: ImageCapture =
            ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

        private val shutterSoundPlayer = ShutterSoundPlayer(context)

        private var lensFacing: LensFacing = LensFacing.BACK
        private var flashMode: FlashMode = FlashMode.OFF
        private var nightModeEnabled: Boolean = false
        private var hdrEnabled: Boolean = false
        private var exposureIndex: Int = 0

        private var extensionsJob: Job? = null
        private var focusJob: Job? = null
        private var orientationJob: Job? = null

        override suspend fun bind(owner: LifecycleOwner) {
            this.owner = owner
            provider =
                provider ?: ProcessCameraProvider.awaitInstance(context).also { provider = it }
            extensionsManager =
                extensionsManager
                    ?: ExtensionsManager
                        .getInstanceAsync(context, provider!!)
                        .await()
                        .also { extensionsManager = it }
            rebindInternal()
            startOrientationObserver()
        }

        private fun startOrientationObserver() {
            if (orientationJob?.isActive == true) return
            orientationJob =
                scope.launch {
                    sensorSession.deviceOrientation.collect { rotation ->
                        imageCapture.targetRotation = rotation
                    }
                }
        }

        private fun scheduleRebind(debounceMs: Long) {
            extensionsJob?.cancel()
            extensionsJob =
                scope.launch {
                    if (debounceMs > 0) delay(debounceMs)
                    rebindInternal()
                }
        }

        private suspend fun rebindInternal() {
            val currentProvider = provider ?: return
            val currentOwner = owner ?: return
            val extManager = extensionsManager ?: return

            currentProvider.unbindAll()

            val baseSelector =
                if (lensFacing == LensFacing.BACK) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

            val nightAvailable = extManager.isExtensionAvailable(baseSelector, ExtensionMode.NIGHT)
            val hdrAvailable = extManager.isExtensionAvailable(baseSelector, ExtensionMode.HDR)

            val cameraSelector =
                when {
                    nightModeEnabled && nightAvailable -> {
                        extManager.getExtensionEnabledCameraSelector(baseSelector, ExtensionMode.NIGHT)
                    }

                    hdrEnabled && hdrAvailable -> {
                        extManager.getExtensionEnabledCameraSelector(baseSelector, ExtensionMode.HDR)
                    }

                    else -> {
                        baseSelector
                    }
                }

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider { request -> _surfaceRequest.value = request }

            applyFlashModeToImageCapture(flashMode)

            val newCamera =
                currentProvider.bindToLifecycle(
                    currentOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                )
            camera = newCamera

            updateCapabilitiesFromInfo(newCamera.cameraInfo, extManager, baseSelector)

            if (flashMode == FlashMode.TORCH) {
                newCamera.cameraControl.enableTorch(true)
            }

            if (exposureIndex != 0) {
                newCamera.cameraControl.setExposureCompensationIndex(exposureIndex)
            }

            newCamera.cameraInfo.zoomState.removeObservers(currentOwner)
            newCamera.cameraInfo.zoomState.observe(currentOwner) { zoom ->
                if (zoom != null) {
                    _zoomState.value =
                        ZoomRange(
                            min = zoom.minZoomRatio,
                            max = zoom.maxZoomRatio,
                            current = zoom.zoomRatio,
                        )
                }
            }
        }

        private fun updateCapabilitiesFromInfo(
            cameraInfo: CameraInfo,
            extManager: ExtensionsManager,
            baseSelector: CameraSelector,
        ) {
            val nightAvailable = extManager.isExtensionAvailable(baseSelector, ExtensionMode.NIGHT)
            val hdrAvailable = extManager.isExtensionAvailable(baseSelector, ExtensionMode.HDR)
            val exposureState = cameraInfo.exposureState
            val range = exposureState.exposureCompensationRange
            val step = exposureState.exposureCompensationStep

            _capabilities.value =
                CameraCapabilities(
                    hasFlash = cameraInfo.hasFlashUnit(),
                    nightModeAvailable = nightAvailable,
                    hdrAvailable = hdrAvailable,
                    exposureIndexMin = range.lower,
                    exposureIndexMax = range.upper,
                    exposureStepNumerator = step.numerator,
                    exposureStepDenominator = step.denominator,
                )
        }

        override suspend fun capture(): Result<String> {
            val capture = imageCapture
            val tempDir = File(context.cacheDir, "temp").also { it.mkdirs() }
            val tempFile = File(tempDir, "capture_${UUID.randomUUID()}.jpg")
            val options = ImageCapture.OutputFileOptions.Builder(tempFile).build()

            return runCatching {
                shutterSoundPlayer.play()
                val savedUri: String =
                    suspendCancellableCoroutine { cont ->
                        capture.takePicture(
                            options,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onError(exception: ImageCaptureException) {
                                    scope.launch(NonCancellable) {
                                        withContext(Dispatchers.IO) {
                                            runCatching { tempFile.delete() }
                                        }
                                    }
                                    cont.resumeWithException(exception)
                                }

                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    val uri =
                                        outputFileResults.savedUri ?: Uri.fromFile(tempFile)
                                    cont.resume(uri.toString())
                                }
                            },
                        )
                        cont.invokeOnCancellation {
                            scope.launch(NonCancellable) {
                                withContext(Dispatchers.IO) {
                                    runCatching { tempFile.delete() }
                                }
                            }
                        }
                    }
                savedUri
            }
        }

        override fun setZoom(ratio: Float) {
            val zs = _zoomState.value
            val clamped = ratio.coerceIn(zs.min, zs.max)
            camera?.cameraControl?.setZoomRatio(clamped)
        }

        override fun setFlash(mode: FlashMode) {
            flashMode = mode
            applyFlashModeToImageCapture(mode)
            camera?.cameraControl?.enableTorch(mode == FlashMode.TORCH)
        }

        private fun applyFlashModeToImageCapture(mode: FlashMode) {
            imageCapture.flashMode =
                when (mode) {
                    FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
                    FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
                    FlashMode.ON -> ImageCapture.FLASH_MODE_ON
                    FlashMode.TORCH -> ImageCapture.FLASH_MODE_OFF
                }
        }

        override fun setLensFacing(facing: LensFacing) {
            if (lensFacing == facing) return
            lensFacing = facing
            scheduleRebind(debounceMs = 0L)
        }

        override fun setNightMode(enabled: Boolean) {
            if (nightModeEnabled == enabled) return
            nightModeEnabled = enabled
            if (enabled) hdrEnabled = false
            scheduleRebind(debounceMs = EXTENSIONS_DEBOUNCE_MS)
        }

        override fun setHdrMode(enabled: Boolean) {
            if (hdrEnabled == enabled) return
            hdrEnabled = enabled
            if (enabled) nightModeEnabled = false
            scheduleRebind(debounceMs = EXTENSIONS_DEBOUNCE_MS)
        }

        override fun startFocusAndMetering(
            x: Float,
            y: Float,
            viewWidth: Float,
            viewHeight: Float,
        ) {
            focusJob?.cancel()
            focusJob =
                scope.launch {
                    delay(FOCUS_DEBOUNCE_MS)
                    val control: CameraControl = camera?.cameraControl ?: return@launch
                    val factory = SurfaceOrientedMeteringPointFactory(viewWidth, viewHeight)
                    val point = factory.createPoint(x, y)
                    val action =
                        FocusMeteringAction
                            .Builder(point)
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                    control.startFocusAndMetering(action)
                }
        }

        override fun setExposureIndex(index: Int) {
            exposureIndex = index
            camera?.cameraControl?.setExposureCompensationIndex(index)
        }

        override fun sensorRotationDegrees(facing: LensFacing): Int {
            camera?.let { c ->
                return c.cameraInfo.sensorRotationDegrees
            }
            val cameraManager =
                context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return 90
            val targetFacing =
                if (facing == LensFacing.BACK) {
                    CameraCharacteristics.LENS_FACING_BACK
                } else {
                    CameraCharacteristics.LENS_FACING_FRONT
                }
            val id =
                runCatching {
                    cameraManager.cameraIdList.firstOrNull { id ->
                        val chars = cameraManager.getCameraCharacteristics(id)
                        chars.get(CameraCharacteristics.LENS_FACING) == targetFacing
                    }
                }.getOrNull() ?: return 90
            return runCatching {
                cameraManager
                    .getCameraCharacteristics(id)
                    .get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
            }.getOrDefault(90)
        }

        override fun playShutterSound() {
            shutterSoundPlayer.play()
        }

        override fun release() {
            extensionsJob?.cancel()
            focusJob?.cancel()
            orientationJob?.cancel()
            provider?.unbindAll()
            shutterSoundPlayer.release()
            camera = null
            _surfaceRequest.value = null
            scope.cancel()
        }

        companion object {
            private const val EXTENSIONS_DEBOUNCE_MS = 300L
            private const val FOCUS_DEBOUNCE_MS = 200L
        }
    }
