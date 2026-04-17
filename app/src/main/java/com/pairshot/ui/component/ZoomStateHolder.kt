package com.pairshot.ui.component

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

data class ZoomUiState(
    val currentRatio: Float = 1f,
    val minRatio: Float = 1f,
    val maxRatio: Float = 1f,
    val presetRatios: List<Float> = listOf(1f),
    /** preset 원본값 → 사용자가 다이얼로 조정한 세부 배율. 예: {1f: 1.2f} */
    val customRatios: Map<Float, Float> = emptyMap(),
)

/**
 * 줌 상태와 로직을 캡슐화하는 순수 클래스.
 * Hilt 주입 불필요 — ViewModel 내부에서 직접 생성한다.
 *
 * Android 프레임워크 의존성 없음: CameraInfo 대신 min/max Float를 받는다.
 */
class ZoomStateHolder {
    private val _zoomUiState = MutableStateFlow(ZoomUiState())
    val zoomUiState: StateFlow<ZoomUiState> = _zoomUiState.asStateFlow()

    fun initFromZoomState(
        minRatio: Float,
        maxRatio: Float,
    ) {
        val presets = buildPresetRatios(minRatio, maxRatio)
        _zoomUiState.update { current ->
            current.copy(
                minRatio = minRatio,
                maxRatio = maxRatio,
                presetRatios = presets,
                customRatios = emptyMap(),
            )
        }
    }

    fun updateZoomRatio(ratio: Float) {
        val current = _zoomUiState.value
        val clamped = ratio.coerceIn(current.minRatio, current.maxRatio)
        _zoomUiState.update { it.copy(currentRatio = clamped) }
    }

    /**
     * 프리셋 버튼 탭 시 호출.
     * - 해당 프리셋에 커스텀 배율이 있으면 → 원래 프리셋값으로 초기화 + 커스텀 제거
     * - 커스텀 없으면 → 해당 프리셋 배율로 이동
     */
    fun onPresetTapped(preset: Float) {
        _zoomUiState.update {
            it.copy(
                currentRatio = preset,
                customRatios = it.customRatios - preset,
            )
        }
    }

    /**
     * 다이얼 드래그 종료 시 호출.
     * currentRatio가 어느 프리셋 구간에 속하는지 판단하여 해당 프리셋의 커스텀 배율로 저장.
     * 프리셋 원본값과 거의 같으면 (±0.05) 커스텀을 제거한다.
     */
    fun applyCustomRatio() {
        val state = _zoomUiState.value
        val nearest = state.presetRatios.minByOrNull { abs(it - state.currentRatio) } ?: return
        val threshold = 0.05f
        _zoomUiState.update {
            if (abs(state.currentRatio - nearest) > threshold) {
                it.copy(customRatios = it.customRatios + (nearest to state.currentRatio))
            } else {
                it.copy(
                    currentRatio = nearest,
                    customRatios = it.customRatios - nearest,
                )
            }
        }
    }

    fun resetZoomForLensSwitch() {
        val current = _zoomUiState.value
        val resetRatio = 1f.coerceIn(current.minRatio, current.maxRatio)
        _zoomUiState.update {
            it.copy(
                currentRatio = resetRatio,
                customRatios = emptyMap(),
            )
        }
    }

    fun restoreZoomForPair(zoomLevel: Float?) {
        val ratio = zoomLevel ?: 1f
        val current = _zoomUiState.value
        val clamped = ratio.coerceIn(current.minRatio, current.maxRatio)
        _zoomUiState.update { it.copy(currentRatio = clamped) }
    }

    private fun buildPresetRatios(
        minRatio: Float,
        maxRatio: Float,
    ): List<Float> {
        val presets = mutableListOf<Float>()
        if (minRatio < 1f) presets.add(minRatio.coerceAtLeast(0.5f))
        presets.add(1f)
        if (maxRatio >= 2f) presets.add(2f)
        if (maxRatio >= 5f) presets.add(5f)
        return presets.distinct().sorted()
    }
}
