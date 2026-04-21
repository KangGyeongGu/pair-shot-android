package com.pairshot.feature.camera.preview

import android.util.Range
import android.util.Rational
import androidx.camera.compose.CameraXViewfinder
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pairshot.feature.camera.component.CameraOverlayLayer
import com.pairshot.feature.camera.component.FocusExposureOverlay
import com.pairshot.feature.camera.component.ZoomControls
import com.pairshot.feature.camera.component.ZoomUiState

@Composable
internal fun CameraPreviewPane(
    surfaceRequest: SurfaceRequest?,
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
    onTapToFocus: (x: Float, y: Float, viewWidth: Int, viewHeight: Int) -> Unit,
    overlayContent: (@Composable () -> Unit)? = null,
) {
    val latestZoomRatio = rememberUpdatedState(zoomUiState.currentRatio)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
                .pointerInput(zoomUiState.minRatio, zoomUiState.maxRatio) {
                    detectTransformGestures { _, _, zoom, _ ->
                        val newRatio =
                            (latestZoomRatio.value * zoom)
                                .coerceIn(zoomUiState.minRatio, zoomUiState.maxRatio)
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

            Box(modifier = previewFrameModifier.align(Alignment.Center).clipToBounds()) {
                CameraOverlayLayer(
                    gridEnabled = gridEnabled,
                    levelEnabled = levelEnabled,
                    roll = roll,
                )

                FocusExposureOverlay(
                    onTapToFocus = onTapToFocus,
                    onExposureReset = onExposureReset,
                    onExposureAdjust = onExposureAdjust,
                    exposureRange = exposureRange,
                    currentExposureIndex = currentExposureIndex,
                    exposureStep = exposureStep,
                    modifier = Modifier.fillMaxSize(),
                )

                ZoomControls(
                    zoomUiState = zoomUiState,
                    onZoomRatioChanged = onZoomRatioChanged,
                    onPresetTapped = onPresetTapped,
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
