package com.pairshot.feature.camera.ui.preview

import android.util.Range
import android.util.Rational
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraControl
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pairshot.feature.camera.ui.component.CameraOverlayLayer
import com.pairshot.feature.camera.ui.component.FocusExposureOverlay
import com.pairshot.feature.camera.ui.component.ZoomControls
import com.pairshot.feature.camera.ui.component.ZoomUiState
import java.util.concurrent.TimeUnit

/**
 * 카메라 프리뷰 영역 — CameraXViewfinder, 블랙아웃 오버레이, 핀치-투-줌, FocusExposureOverlay,
 * ZoomControls, 격자/수평계 오버레이 포함.
 *
 * @param overlayContent 화면별 추가 오버레이 슬롯 (예: AfterCamera 의 OverlayGuide). null이면 생략.
 */
@Composable
internal fun CameraPreviewPane(
    surfaceRequest: SurfaceRequest?,
    cameraControl: CameraControl?,
    zoomUiState: ZoomUiState,
    blackoutAlpha: Float,
    gridEnabled: Boolean,
    levelEnabled: Boolean,
    roll: Float,
    exposureRange: Range<Int>,
    currentExposureIndex: Int,
    exposureStep: Rational,
    height: Dp,
    onZoomRatioChanged: (Float) -> Unit,
    onPresetTapped: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onExposureReset: () -> Unit,
    onExposureAdjust: (Int) -> Unit,
    overlayContent: (@Composable () -> Unit)? = null,
) {
    val latestZoomRatio = rememberUpdatedState(zoomUiState.currentRatio)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
                .pointerInput(cameraControl, zoomUiState.minRatio, zoomUiState.maxRatio) {
                    detectTransformGestures { _, _, zoom, _ ->
                        val control = cameraControl ?: return@detectTransformGestures
                        val newRatio =
                            (latestZoomRatio.value * zoom)
                                .coerceIn(zoomUiState.minRatio, zoomUiState.maxRatio)
                        control.setZoomRatio(newRatio)
                        onZoomRatioChanged(newRatio)
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

        overlayContent?.invoke()

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
            onExposureReset = onExposureReset,
            onExposureAdjust = onExposureAdjust,
            exposureRange = exposureRange,
            currentExposureIndex = currentExposureIndex,
            exposureStep = exposureStep,
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
                CameraOverlayLayer(
                    gridEnabled = gridEnabled,
                    levelEnabled = levelEnabled,
                    roll = roll,
                )

                ZoomControls(
                    zoomUiState = zoomUiState,
                    onZoomRatioChanged = { newRatio ->
                        cameraControl?.setZoomRatio(newRatio)
                        onZoomRatioChanged(newRatio)
                    },
                    onPresetTapped = { preset ->
                        onPresetTapped(preset)
                        cameraControl?.setZoomRatio(zoomUiState.currentRatio)
                    },
                    onDragEnd = onDragEnd,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                )
            }
        }
    }
}
