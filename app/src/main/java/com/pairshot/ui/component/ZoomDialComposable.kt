package com.pairshot.ui.component

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 줌 컨트롤: 프리셋 버튼 카드 ↔ 수평 다이얼 전환.
 *
 * - 기본: 프리셋 버튼들이 하나의 pill 카드 안에 나란히 표시
 * - 카드 영역 드래그 시: 카드가 위로 페이드아웃, 다이얼이 아래에서 페이드인
 * - 드래그 종료: 다이얼 사라지고 카드 복귀, 조정된 배율이 가장 가까운 프리셋 버튼에 반영
 * - 커스텀 배율 버튼 탭: 원래 프리셋으로 초기화
 */
@Composable
fun ZoomControls(
    zoomUiState: ZoomUiState,
    onZoomRatioChanged: (Float) -> Unit,
    onPresetTapped: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
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
    // 햅틱용: 현재 0.1x 단위 틱 인덱스 추적
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

                            // 0.1x 틱을 넘을 때마다 햅틱 피드백
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
                    // 카드→다이얼: 다이얼 아래에서 페이드인, 카드 위로 페이드아웃
                    (fadeIn() + slideInVertically { it / 2 })
                        .togetherWith(fadeOut() + slideOutVertically { -it / 2 })
                } else {
                    // 다이얼→카드: 카드 위에서 페이드인, 다이얼 아래로 페이드아웃
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
    }
}

/**
 * 프리셋 버튼들을 하나의 pill 카드 안에 배치.
 * 현재 배율에 가장 가까운 프리셋이 하이라이트된다.
 * 커스텀 배율이 있으면 해당 프리셋 버튼의 수치가 변경되어 표시된다.
 */
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
                    color = Color.Black.copy(alpha = 0.35f),
                    shape = shape,
                ).padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        zoomUiState.presetRatios.forEach { preset ->
            val isActive = preset == activePreset
            val displayRatio = zoomUiState.customRatios[preset] ?: preset
            val bgColor = if (isActive) primary else Color.Transparent
            val textColor = if (isActive) onPrimary else Color.White.copy(alpha = 0.7f)

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

/**
 * 다이얼 + 현재 배율 텍스트 (드래그 중 표시).
 * 다이얼은 순수 시각 표현 — 드래그 제스처는 상위 ZoomControls에서 처리.
 */
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
        // 현재 배율 텍스트 (반투명 카드)
        Box(
            modifier =
                Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.35f),
                        shape = MaterialTheme.shapes.small,
                    ).padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = formatZoomLabel(zoomUiState.currentRatio),
                color = Color.White,
                style = labelStyle,
                fontWeight = FontWeight.Medium,
            )
        }

        // 눈금 캔버스
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

            // 정수 기반 반복으로 float 누적 오차 제거
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
                val tickColor = if (isMajor) Color.White else Color.White.copy(alpha = 0.5f)
                val topY = canvasH - tickHeightPx

                drawLine(
                    color = tickColor,
                    start = Offset(x, topY),
                    end = Offset(x, canvasH),
                    strokeWidth = tickWidthPx,
                )
            }

            // 중앙 인디케이터
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
