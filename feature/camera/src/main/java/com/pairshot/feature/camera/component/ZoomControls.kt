package com.pairshot.feature.camera.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.pairshot.core.designsystem.PairShotCameraTokens
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pairshot.feature.camera.R
import kotlin.math.abs
import kotlin.math.roundToInt

private val LensButtonSize = 36.dp
private val LensButtonIconSize = 18.dp

@Composable
fun ZoomControls(
    zoomUiState: ZoomUiState,
    onZoomRatioChanged: (Float) -> Unit,
    onPresetTapped: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleLens: (() -> Unit)? = null,
) {
    var isDragging by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val latestRatio by rememberUpdatedState(zoomUiState.currentRatio)
    val latestOnZoomChanged by rememberUpdatedState(onZoomRatioChanged)
    val latestOnDragEnd by rememberUpdatedState(onDragEnd)

    val rangeSpanDp = 300f
    val rangeSpanPx = with(density) { rangeSpanDp.dp.toPx() }
    val zoomRange = (zoomUiState.maxRatio - zoomUiState.minRatio).coerceAtLeast(0.01f)
    val pxPerZoom = rangeSpanPx / zoomRange

    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    var lastTickIndex by remember { mutableIntStateOf((zoomUiState.currentRatio * 10).roundToInt()) }

    Box(
        modifier =
            modifier
                .pointerInput(zoomUiState.minRatio, zoomUiState.maxRatio) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragAccumulator = 0f
                            lastTickIndex = (latestRatio * 10).roundToInt()
                        },
                        onDragEnd = {
                            isDragging = false
                            latestOnDragEnd()
                        },
                        onDragCancel = {
                            isDragging = false
                            latestOnDragEnd()
                        },
                    ) { _, dragAmount ->
                        dragAccumulator -= dragAmount
                        val deltaZoom = dragAccumulator / pxPerZoom
                        if (abs(deltaZoom) >= 0.05f) {
                            val newRatio =
                                (latestRatio + deltaZoom)
                                    .coerceIn(zoomUiState.minRatio, zoomUiState.maxRatio)
                            latestOnZoomChanged(newRatio)
                            dragAccumulator = 0f

                            val newTickIndex = (newRatio * 10).roundToInt()
                            if (newTickIndex != lastTickIndex) {
                                val isMajorTick = newTickIndex % 10 == 0
                                if (isMajorTick) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                lastTickIndex = newTickIndex
                            }
                        }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = isDragging,
            transitionSpec = {
                if (targetState) {
                    (fadeIn() + slideInVertically { it / 2 })
                        .togetherWith(fadeOut() + slideOutVertically { -it / 2 })
                } else {
                    (fadeIn() + slideInVertically { -it / 2 })
                        .togetherWith(fadeOut() + slideOutVertically { it / 2 })
                }.using(SizeTransform(clip = false) { _, _ -> snap() })
            },
            contentAlignment = Alignment.Center,
            label = "zoom-control-switch",
        ) { dragging ->
            if (dragging) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    ZoomDialWithLabel(zoomUiState = zoomUiState, pxPerZoom = pxPerZoom)
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    ZoomPresetCard(
                        zoomUiState = zoomUiState,
                        onPresetTapped = onPresetTapped,
                    )
                }
            }
        }

        if (onToggleLens != null) {
            val toggleLens: () -> Unit = onToggleLens
            AnimatedVisibility(
                visible = !isDragging,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(LensButtonSize)
                            .clip(CircleShape)
                            .background(PairShotCameraTokens.Letterbox.copy(alpha = 0.35f))
                            .clickable(onClick = toggleLens),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = stringResource(R.string.camera_desc_switch),
                        tint = PairShotCameraTokens.Foreground,
                        modifier = Modifier.size(LensButtonIconSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomPresetCard(
    zoomUiState: ZoomUiState,
    onPresetTapped: (Float) -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val labelStyle = MaterialTheme.typography.labelMedium
    val shape = MaterialTheme.shapes.small

    val activePreset =
        zoomUiState.presetRatios
            .minByOrNull { abs(it - zoomUiState.currentRatio) }

    Row(
        modifier =
            Modifier
                .background(
                    color = PairShotCameraTokens.Letterbox.copy(alpha = 0.35f),
                    shape = shape,
                ).padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        zoomUiState.presetRatios.forEach { preset ->
            val isActive = preset == activePreset
            val displayRatio = zoomUiState.customRatios[preset] ?: preset
            val bgColor = if (isActive) primary else Color.Transparent
            val textColor = if (isActive) onPrimary else PairShotCameraTokens.Foreground.copy(alpha = 0.7f)

            Box(
                modifier =
                    Modifier
                        .height(28.dp)
                        .background(color = bgColor, shape = shape)
                        .clickable { onPresetTapped(preset) }
                        .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = formatZoomLabel(displayRatio),
                    color = textColor,
                    style = labelStyle,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ZoomDialWithLabel(
    zoomUiState: ZoomUiState,
    pxPerZoom: Float,
) {
    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelStyle = MaterialTheme.typography.labelSmall

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .background(
                        color = PairShotCameraTokens.Letterbox.copy(alpha = 0.35f),
                        shape = MaterialTheme.shapes.small,
                    ).padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = formatZoomLabel(zoomUiState.currentRatio),
                color = PairShotCameraTokens.Foreground,
                style = labelStyle,
                fontWeight = FontWeight.Medium,
            )
        }

        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(20.dp),
        ) {
            val centerX = size.width / 2f
            val canvasH = size.height

            val visibleZoomSpan = size.width / pxPerZoom
            val visibleMin =
                (zoomUiState.currentRatio - visibleZoomSpan / 2)
                    .coerceAtLeast(zoomUiState.minRatio)
            val visibleMax =
                (zoomUiState.currentRatio + visibleZoomSpan / 2)
                    .coerceAtMost(zoomUiState.maxRatio)

            val startTick = (visibleMin * 10).toInt()
            val endTick = ((visibleMax * 10).toInt()) + 1
            for (i in startTick..endTick) {
                val tick = i / 10f
                val offset = (tick - zoomUiState.currentRatio) * pxPerZoom
                val x = centerX + offset
                if (x < 0f || x > size.width) continue

                val isMajor = i % 10 == 0
                val tickHeightPx =
                    if (isMajor) with(density) { 12.dp.toPx() } else with(density) { 6.dp.toPx() }
                val tickWidthPx =
                    if (isMajor) with(density) { 2.dp.toPx() } else with(density) { 1.dp.toPx() }
                val tickColor = if (isMajor) PairShotCameraTokens.Foreground else PairShotCameraTokens.Foreground.copy(alpha = 0.5f)
                val topY = canvasH - tickHeightPx

                drawLine(
                    color = tickColor,
                    start = Offset(x, topY),
                    end = Offset(x, canvasH),
                    strokeWidth = tickWidthPx,
                )
            }

            val indicatorH = with(density) { 18.dp.toPx() }
            val indicatorW = with(density) { 2.dp.toPx() }
            drawLine(
                color = primaryColor,
                start = Offset(centerX, canvasH - indicatorH),
                end = Offset(centerX, canvasH),
                strokeWidth = indicatorW,
            )
        }
    }
}

private fun formatZoomLabel(ratio: Float): String =
    when {
        ratio == ratio.roundToInt().toFloat() -> "${ratio.roundToInt()}x"
        else -> "${"%.1f".format(ratio)}x"
    }
