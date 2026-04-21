package com.pairshot.feature.camera.ui.viewmodel

import android.util.Range
import android.util.Rational
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.core.domain.capture.SaveAfterPhotoUseCase
import com.pairshot.core.domain.pair.GetPairsByProjectUseCase
import com.pairshot.core.domain.pair.GetUnpairedPhotosUseCase
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.infra.camera.CameraSession
import com.pairshot.core.infra.sensor.SensorSession
import com.pairshot.core.model.FlashMode
import com.pairshot.core.model.LensFacing
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.rendering.OverlayTransformCalculator
import com.pairshot.feature.camera.ui.component.ZoomStateHolder
import com.pairshot.feature.camera.ui.component.ZoomUiState
import com.pairshot.feature.camera.ui.state.CameraCapabilities
import com.pairshot.feature.camera.ui.state.CameraSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface AfterCameraEvent {
    data object AllCompleted : AfterCameraEvent

    data class AfterSaved(
        val pairId: Long,
    ) : AfterCameraEvent

    data class CaptureError(
        val message: String,
    ) : AfterCameraEvent

    data class SaveError(
        val message: String,
    ) : AfterCameraEvent
}

@HiltViewModel
class AfterCameraViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val cameraSession: CameraSession,
        private val sensorSession: SensorSession,
        getPairsByProjectUseCase: GetPairsByProjectUseCase,
        getUnpairedPhotosUseCase: GetUnpairedPhotosUseCase,
        private val saveAfterPhotoUseCase: SaveAfterPhotoUseCase,
        private val appSettingsRepository: AppSettingsRepository,
    ) : ViewModel() {
        private val projectId: Long = savedStateHandle["projectId"] ?: 0L
        private val initialPairId: Long? = savedStateHandle["initialPairId"]

        val totalPairCount: StateFlow<Int> =
            getPairsByProjectUseCase(projectId)
                .map { pairs -> pairs.size }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

        val unpairedPhotos: StateFlow<List<PhotoPair>> =
            getUnpairedPhotosUseCase(projectId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        private val _currentIndex = MutableStateFlow(0)
        val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

        private val _lensFacing = MutableStateFlow(LensFacing.BACK)
        val lensFacing: StateFlow<LensFacing> = _lensFacing.asStateFlow()

        private val zoomHolder = ZoomStateHolder()
        val zoomUiState: StateFlow<ZoomUiState> = zoomHolder.zoomUiState

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        private val _overlayEnabled = MutableStateFlow(true)
        val overlayEnabled: StateFlow<Boolean> = _overlayEnabled.asStateFlow()

        private val _overlayAlpha = MutableStateFlow(0.5f)
        val overlayAlpha: StateFlow<Float> = _overlayAlpha.asStateFlow()

        private val _settingsState = MutableStateFlow(CameraSettingsState())
        val settingsState: StateFlow<CameraSettingsState> = _settingsState.asStateFlow()

        val surfaceRequest: StateFlow<SurfaceRequest?> = cameraSession.surfaceRequest

        val capabilities: StateFlow<CameraCapabilities> =
            cameraSession.capabilities
                .map { caps ->
                    CameraCapabilities(
                        hasFlash = caps.hasFlash,
                        nightModeAvailable = caps.nightModeAvailable,
                        hdrAvailable = caps.hdrAvailable,
                        exposureRange = Range(caps.exposureIndexMin, caps.exposureIndexMax),
                        exposureStep = Rational(caps.exposureStepNumerator, caps.exposureStepDenominator),
                    )
                }.stateIn(viewModelScope, SharingStarted.Eagerly, CameraCapabilities())

        val roll: StateFlow<Float> = sensorSession.roll

        private val _events = MutableSharedFlow<AfterCameraEvent>()
        val events: SharedFlow<AfterCameraEvent> = _events.asSharedFlow()

        private var initialIndexSet = false

        init {
            viewModelScope.launch {
                val s = appSettingsRepository.settingsFlow.first()
                val initial =
                    CameraSettingsState(
                        gridEnabled = s.cameraGridEnabled,
                        levelEnabled = s.cameraLevelEnabled,
                        flashMode = runCatching { FlashMode.valueOf(s.cameraFlashMode) }.getOrDefault(FlashMode.OFF),
                        nightModeEnabled = s.cameraNightModeEnabled,
                        hdrEnabled = s.cameraHdrEnabled,
                    )
                _settingsState.value = initial
                _overlayEnabled.value = s.overlayEnabled
                _overlayAlpha.value = s.defaultOverlayAlpha.coerceIn(0f, 1f)
                cameraSession.setFlash(initial.flashMode)
                cameraSession.setNightMode(initial.nightModeEnabled)
                cameraSession.setHdrMode(initial.hdrEnabled)
                sensorSession.setLevelEnabled(initial.levelEnabled)
            }
            viewModelScope.launch {
                cameraSession.capabilities.collect { caps ->
                    val state = _settingsState.value
                    var changed = state
                    if (!caps.hasFlash && state.flashMode != FlashMode.OFF) {
                        changed = changed.copy(flashMode = FlashMode.OFF)
                        cameraSession.setFlash(FlashMode.OFF)
                    }
                    if (!caps.nightModeAvailable && state.nightModeEnabled) {
                        changed = changed.copy(nightModeEnabled = false)
                        cameraSession.setNightMode(false)
                    }
                    if (!caps.hdrAvailable && state.hdrEnabled) {
                        changed = changed.copy(hdrEnabled = false)
                        cameraSession.setHdrMode(false)
                    }
                    if (changed !== state) _settingsState.value = changed
                }
            }
            viewModelScope.launch {
                cameraSession.zoomState.collect { zoom ->
                    zoomHolder.initFromZoomState(zoom.min, zoom.max)
                    val pair = unpairedPhotos.value.getOrNull(_currentIndex.value)
                    if (pair?.zoomLevel != null) {
                        zoomHolder.restoreZoomForPair(pair.zoomLevel)
                        cameraSession.setZoom(zoomHolder.zoomUiState.value.currentRatio)
                    }
                }
            }
        }

        suspend fun bind(owner: LifecycleOwner) {
            sensorSession.bind(owner)
            cameraSession.bind(owner)
        }

        fun updateZoomRatio(ratio: Float) {
            zoomHolder.updateZoomRatio(ratio)
            cameraSession.setZoom(ratio)
        }

        fun onPresetTapped(preset: Float) {
            zoomHolder.onPresetTapped(preset)
            cameraSession.setZoom(zoomHolder.zoomUiState.value.currentRatio)
        }

        fun applyCustomRatio() {
            zoomHolder.applyCustomRatio()
        }

        fun resetZoomForLensSwitch() {
            zoomHolder.resetZoomForLensSwitch()
            cameraSession.setZoom(zoomHolder.zoomUiState.value.currentRatio)
        }

        fun restoreZoomForPair(zoomLevel: Float?) {
            zoomHolder.restoreZoomForPair(zoomLevel)
            cameraSession.setZoom(zoomHolder.zoomUiState.value.currentRatio)
        }

        fun overlayRotation(): Float {
            val pair = unpairedPhotos.value.getOrNull(_currentIndex.value) ?: return 0f
            val exifDegrees = 0
            val sensor = cameraSession.sensorRotationDegrees(_lensFacing.value)
            return OverlayTransformCalculator.calculate(sensor, exifDegrees)
        }

        fun onUnpairedPhotosUpdated(photos: List<PhotoPair>) {
            if (!initialIndexSet && photos.isNotEmpty() && initialPairId != null) {
                val idx = photos.indexOfFirst { it.id == initialPairId }
                if (idx >= 0) _currentIndex.value = idx
                initialIndexSet = true
            }
            if (photos.isNotEmpty() && _currentIndex.value >= photos.size) {
                _currentIndex.value = photos.size - 1
            }
        }

        fun selectIndex(index: Int) {
            _currentIndex.value = index
        }

        fun moveToNext() {
            val photos = unpairedPhotos.value
            if (_currentIndex.value < photos.size - 1) {
                _currentIndex.value++
            }
        }

        fun moveToPrevious() {
            if (_currentIndex.value > 0) {
                _currentIndex.value--
            }
        }

        fun onAfterCaptured() {
            val photos = unpairedPhotos.value
            if (photos.isEmpty() || _isSaving.value) return
            val currentPair = photos.getOrNull(_currentIndex.value) ?: return

            viewModelScope.launch {
                _isSaving.value = true
                val captureResult = cameraSession.capture()
                val tempUri = captureResult.getOrNull()
                if (captureResult.isFailure || tempUri == null) {
                    _events.emit(
                        AfterCameraEvent.CaptureError(
                            captureResult.exceptionOrNull()?.message ?: "촬영 실패",
                        ),
                    )
                    _isSaving.value = false
                    return@launch
                }
                try {
                    saveAfterPhotoUseCase(
                        pairId = currentPair.id,
                        tempFileUri = tempUri,
                    )
                    _events.emit(AfterCameraEvent.AfterSaved(currentPair.id))
                } catch (e: Exception) {
                    _events.emit(AfterCameraEvent.SaveError(e.message ?: "저장에 실패했습니다."))
                } finally {
                    try {
                        val path = java.net.URI(tempUri).path
                        if (path != null) java.io.File(path).delete()
                    } catch (e: Exception) {
                        Timber.d(e, "임시 파일 삭제 실패: $tempUri")
                    }
                    _isSaving.value = false
                }
            }
        }

        fun onFocusRequested(
            x: Float,
            y: Float,
            viewWidth: Float,
            viewHeight: Float,
        ) {
            cameraSession.startFocusAndMetering(x, y, viewWidth, viewHeight)
        }

        fun emitAllCompleted() {
            viewModelScope.launch {
                _events.emit(AfterCameraEvent.AllCompleted)
            }
        }

        fun toggleOverlay() {
            val newValue = !_overlayEnabled.value
            _overlayEnabled.value = newValue
            viewModelScope.launch {
                appSettingsRepository.updateOverlayEnabled(newValue)
            }
        }

        fun updateOverlayAlpha(alpha: Float) {
            val coerced = alpha.coerceIn(0f, 1f)
            _overlayAlpha.value = coerced
            viewModelScope.launch {
                appSettingsRepository.updateOverlayAlpha(coerced)
            }
        }

        fun toggleLensFacing() {
            val next = if (_lensFacing.value == LensFacing.BACK) LensFacing.FRONT else LensFacing.BACK
            _lensFacing.value = next
            cameraSession.setLensFacing(next)
            resetZoomForLensSwitch()
        }

        fun toggleGrid() {
            _settingsState.update { it.copy(gridEnabled = !it.gridEnabled) }
            persistSettings()
        }

        fun toggleLevel() {
            val next = !_settingsState.value.levelEnabled
            _settingsState.update { it.copy(levelEnabled = next) }
            sensorSession.setLevelEnabled(next)
            persistSettings()
        }

        fun cycleFlash() {
            _settingsState.update {
                val next =
                    when (it.flashMode) {
                        FlashMode.OFF -> FlashMode.AUTO
                        FlashMode.AUTO -> FlashMode.ON
                        FlashMode.ON -> FlashMode.TORCH
                        FlashMode.TORCH -> FlashMode.OFF
                    }
                it.copy(flashMode = next)
            }
            cameraSession.setFlash(_settingsState.value.flashMode)
            persistSettings()
        }

        fun toggleNightMode() {
            val next = !_settingsState.value.nightModeEnabled
            _settingsState.update {
                it.copy(
                    nightModeEnabled = next,
                    hdrEnabled = if (next) false else it.hdrEnabled,
                )
            }
            cameraSession.setNightMode(next)
            if (next) cameraSession.setHdrMode(false)
            persistSettings()
        }

        fun toggleHdr() {
            val next = !_settingsState.value.hdrEnabled
            _settingsState.update {
                it.copy(
                    hdrEnabled = next,
                    nightModeEnabled = if (next) false else it.nightModeEnabled,
                )
            }
            cameraSession.setHdrMode(next)
            if (next) cameraSession.setNightMode(false)
            persistSettings()
        }

        fun setExposureIndex(index: Int) {
            _settingsState.update { it.copy(exposureIndex = index) }
            cameraSession.setExposureIndex(index)
        }

        fun toggleSettingsPanel() {
            _settingsState.update { it.copy(showPanel = !it.showPanel) }
        }

        fun dismissSettingsPanel() {
            _settingsState.update { it.copy(showPanel = false) }
        }

        private fun persistSettings() {
            val state = _settingsState.value
            viewModelScope.launch {
                appSettingsRepository.updateCameraGridEnabled(state.gridEnabled)
                appSettingsRepository.updateCameraLevelEnabled(state.levelEnabled)
                appSettingsRepository.updateCameraFlashMode(state.flashMode.name)
                appSettingsRepository.updateCameraNightMode(state.nightModeEnabled)
                appSettingsRepository.updateCameraHdr(state.hdrEnabled)
            }
        }

        override fun onCleared() {
            super.onCleared()
            cameraSession.release()
            sensorSession.release()
        }
    }
