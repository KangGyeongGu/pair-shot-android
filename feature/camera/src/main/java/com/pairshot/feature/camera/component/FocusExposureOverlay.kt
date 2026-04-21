package com.pairshot.feature.camera.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val FOCUS_RING_SIZE = 64.dp
private val FOCUS_RING_STROKE = 1.dp
private val EV_BAR_HEIGHT = 100.dp
private val EV_BAR_WIDTH = 1.5.dp
private val EV_SUN_SIZE = 12.dp
private const val DRAG_DP_PER_EV_STEP = 30f

@Composable
fun FocusExposureOverlay(
    onTapToFocus: (x: Float, y: Float, viewWidth: Int, viewHeight: Int) -> Unit,
    onExposureReset: () -> Unit,
    onExposureAdjust: (newIndex: Int) -> Unit,
    exposureIndexMin: Int,
    exposureIndexMax: Int,
    currentExposureIndex: Int,
    exposureStepNumerator: Int,
    exposureStepDenominator: Int,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var focusPosition by remember { mutableStateOf<Offset?>(null) }
    val ringAlpha = remember { Animatable(0f) }
    val ringScale = remember { Animatable(1.3f) }

    var showEvBar by remember { mutableStateOf(false) }
    var localEvIndex by remember { mutableIntStateOf(currentExposureIndex) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var dragStartEvIndex by remember { mutableIntStateOf(currentExposureIndex) }

    var autoHideJob by remember { mutableStateOf<Job?>(null) }

    val exposureEnabled = exposureIndexMin < exposureIndexMax
    val dragThresholdPx = with(density) { DRAG_DP_PER_EV_STEP.dp.toPx() }
    val focusRingSizePx = with(density) { FOCUS_RING_SIZE.toPx() }
    val focusRingStrokePx = with(density) { FOCUS_RING_STROKE.toPx() }

    fun scheduleAutoHide() {
        autoHideJob?.cancel()
        autoHideJob =
            scope.launch {
                delay(2000L)
                showEvBar = false
                ringAlpha.animateTo(0f, animationSpec = tween(400))
                focusPosition = null
            }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .pointerInput(exposureIndexMin, exposureIndexMax, currentExposureIndex) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val tapPosition = down.position

                        var isDrag = false
                        var pointerReleased = false

                        while (!pointerReleased) {
                            val event = awaitPointerEvent()

                            if (event.changes.size >= 2) return@awaitEachGesture

                            val change = event.changes.firstOrNull() ?: break

                            if (event.type == PointerEventType.Release || !change.pressed) {
                                pointerReleased = true
                                break
                            }

                            val delta = change.position - change.previousPosition
                            if (abs(delta.x) > 3f || abs(delta.y) > 3f) {
                                isDrag = true
                            }
                        }

                        if (isDrag) {
                            if (focusPosition != null && showEvBar && exposureEnabled) {
                            }
                        } else {
                            autoHideJob?.cancel()
                            focusPosition = tapPosition
                            localEvIndex = 0
                            dragStartEvIndex = 0
                            totalDragY = 0f
                            onExposureReset()

                            scope.launch {
                                ringAlpha.snapTo(0f)
                                ringScale.snapTo(1.3f)
                                ringAlpha.animateTo(1f, animationSpec = tween(150))
                                ringScale.animateTo(1f, animationSpec = tween(200))
                            }
                            if (exposureEnabled) {
                                showEvBar = true
                            }
                            onTapToFocus(tapPosition.x, tapPosition.y, size.width, size.height)
                            scheduleAutoHide()
                        }
                    }
                }.pointerInput(showEvBar, exposureIndexMin, exposureIndexMax) {
                    if (!showEvBar || !exposureEnabled) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val focusPos = focusPosition ?: return@awaitEachGesture

                        val ringRadius = focusRingSizePx / 2f
                        val barX = focusPos.x + ringRadius + with(density) { 16.dp.toPx() }
                        val hitAreaLeft = focusPos.x - ringRadius - with(density) { 20.dp.toPx() }
                        val hitAreaRight = barX + with(density) { 30.dp.toPx() }
                        val hitAreaTop = focusPos.y - with(density) { EV_BAR_HEIGHT.toPx() / 2f + 20.dp.toPx() }
                        val hitAreaBottom = focusPos.y + with(density) { EV_BAR_HEIGHT.toPx() / 2f + 20.dp.toPx() }

                        val inHitArea =
                            down.position.x in hitAreaLeft..hitAreaRight &&
                                down.position.y in hitAreaTop..hitAreaBottom

                        if (!inHitArea) return@awaitEachGesture

                        autoHideJob?.cancel()
                        dragStartEvIndex = localEvIndex
                        totalDragY = 0f

                        var pointerReleased = false
                        while (!pointerReleased) {
                            val event = awaitPointerEvent()
                            if (event.changes.size >= 2) break
                            val change = event.changes.firstOrNull() ?: break

                            if (event.type == PointerEventType.Release || !change.pressed) {
                                pointerReleased = true
                                break
                            }

                            totalDragY += (change.position - change.previousPosition).y
                            val evSteps = -(totalDragY / dragThresholdPx).roundToInt()
                            val newIndex =
                                (dragStartEvIndex + evSteps)
                                    .coerceIn(exposureIndexMin, exposureIndexMax)
                            if (newIndex != localEvIndex) {
                                localEvIndex = newIndex
                                onExposureAdjust(newIndex)
                            }

                            change.consume()
                        }

                        scheduleAutoHide()
                    }
                },
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val focusPos = focusPosition
        if (focusPos != null && ringAlpha.value > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val alpha = ringAlpha.value
                val scale = ringScale.value
                val radius = (focusRingSizePx / 2f) * scale

                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = radius,
                    center = focusPos,
                    style = Stroke(width = focusRingStrokePx),
                )

                if (showEvBar && exposureEnabled) {
                    val barHeightPx = with(density) { EV_BAR_HEIGHT.toPx() }
                    val barWidthPx = with(density) { EV_BAR_WIDTH.toPx() }
                    val sunSizePx = with(density) { EV_SUN_SIZE.toPx() }

                    val barX = focusPos.x + radius + with(density) { 16.dp.toPx() }
                    val barTop = focusPos.y - barHeightPx / 2f
                    val barBottom = focusPos.y + barHeightPx / 2f

                    drawLine(
                        color = Color.White.copy(alpha = alpha * 0.7f),
                        start = Offset(barX, barTop),
                        end = Offset(barX, barBottom),
                        strokeWidth = barWidthPx,
                    )

                    val evRangeSize = (exposureIndexMax - exposureIndexMin).coerceAtLeast(1)
                    val evFraction = (localEvIndex - exposureIndexMin).toFloat() / evRangeSize
                    val sunY = barBottom - (evFraction * barHeightPx)

                    drawCircle(
                        color = primaryColor.copy(alpha = alpha),
                        radius = sunSizePx / 2f,
                        center = Offset(barX, sunY),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = sunSizePx / 2f,
                        center = Offset(barX, sunY),
                        style = Stroke(width = with(density) { 1.dp.toPx() }),
                    )
                }
            }

            if (showEvBar && exposureEnabled && ringAlpha.value > 0.3f) {
                val evValue = localEvIndex * (exposureStepNumerator.toFloat() / exposureStepDenominator.toFloat())
                val evText =
                    when {
                        evValue > 0f -> "EV +%.1f".format(evValue)
                        evValue < 0f -> "EV %.1f".format(evValue)
                        else -> "EV 0"
                    }
                val ringRadiusDp = with(density) { ((focusRingSizePx / 2f) * ringScale.value).toDp() }
                val textOffsetX = with(density) { focusPos.x.toDp() } - ringRadiusDp
                val textOffsetY = with(density) { focusPos.y.toDp() } + ringRadiusDp + 6.dp

                Text(
                    text = evText,
                    color = Color.White.copy(alpha = ringAlpha.value),
                    fontSize = 12.sp,
                    modifier =
                        Modifier.offset {
                            IntOffset(
                                x = textOffsetX.roundToPx(),
                                y = textOffsetY.roundToPx(),
                            )
                        },
                )
            }
        }
    }
}
