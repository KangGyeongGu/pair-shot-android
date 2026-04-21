package com.pairshot.feature.camera.component

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
    val customRatios: Map<Float, Float> = emptyMap(),
)

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

    fun onPresetTapped(preset: Float) {
        _zoomUiState.update {
            it.copy(
                currentRatio = preset,
                customRatios = it.customRatios - preset,
            )
        }
    }

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
