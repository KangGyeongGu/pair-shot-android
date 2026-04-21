package com.pairshot.feature.camera.ui.viewmodel

import android.util.Range
import android.util.Rational
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.core.domain.capture.SaveBeforePhotoUseCase
import com.pairshot.core.domain.pair.GetPairsByProjectUseCase
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.infra.camera.CameraSession
import com.pairshot.core.infra.sensor.SensorSession
import com.pairshot.core.model.FlashMode
import com.pairshot.core.model.LensFacing
import com.pairshot.feature.camera.ui.component.ZoomStateHolder
import com.pairshot.feature.camera.ui.component.ZoomUiState
import com.pairshot.feature.camera.ui.state.CameraCapabilities
import com.pairshot.feature.camera.ui.state.CameraSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

sealed interface CameraEvent {
    data class PhotoSaved(
        val pairId: Long,
    ) : CameraEvent

    data class CaptureError(
        val message: String,
    ) : CameraEvent

    data class SaveError(
        val message: String,
    ) : CameraEvent
}

@HiltViewModel
class CameraViewModel
    @Inject
    constructor(
        private val cameraSession: CameraSession,
        private val sensorSession: SensorSession,
        private val saveBeforePhotoUseCase: SaveBeforePhotoUseCase,
        private val getPairsByProjectUseCase: GetPairsByProjectUseCase,
        private val appSettingsRepository: AppSettingsRepository,
    ) : ViewModel() {
        private val _events = MutableSharedFlow<CameraEvent>()
        val events: SharedFlow<CameraEvent> = _events.asSharedFlow()

        private val _lensFacing = MutableStateFlow(LensFacing.BACK)
        val lensFacing: StateFlow<LensFacing> = _lensFacing.asStateFlow()

        private val zoomHolder = ZoomStateHolder()
        val zoomUiState: StateFlow<ZoomUiState> = zoomHolder.zoomUiState

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        private val _beforePreviewUris = MutableStateFlow<List<String>>(emptyList())
        val beforePreviewUris: StateFlow<List<String>> = _beforePreviewUris.asStateFlow()

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
                }
            }
        }

        suspend fun bind(owner: LifecycleOwner) {
            sensorSession.bind(owner)
            cameraSession.bind(owner)
        }

        private var observedProjectId: Long? = null
        private var observeProjectJob: Job? = null

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

        fun onShutterClick(projectId: Long) {
            if (_isSaving.value) return
            viewModelScope.launch {
                _isSaving.value = true
                val captureResult = cameraSession.capture()
                val tempUri = captureResult.getOrNull()
                if (captureResult.isFailure || tempUri == null) {
                    _events.emit(
                        CameraEvent.CaptureError(
                            captureResult.exceptionOrNull()?.message ?: "촬영 실패",
                        ),
                    )
                    _isSaving.value = false
                    return@launch
                }
                try {
                    val pairId =
                        saveBeforePhotoUseCase(
                            projectId = projectId,
                            tempFileUri = tempUri,
                            zoomLevel = zoomHolder.zoomUiState.value.currentRatio,
                            lensId = null,
                        )
                    _events.emit(CameraEvent.PhotoSaved(pairId))
                } catch (e: Exception) {
                    _events.emit(CameraEvent.SaveError(e.message ?: "저장에 실패했습니다."))
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

        fun toggleLensFacing() {
            val next = if (_lensFacing.value == LensFacing.BACK) LensFacing.FRONT else LensFacing.BACK
            _lensFacing.value = next
            cameraSession.setLensFacing(next)
            resetZoomForLensSwitch()
        }

        fun observeProject(projectId: Long) {
            if (observedProjectId == projectId && observeProjectJob?.isActive == true) return

            observedProjectId = projectId
            observeProjectJob?.cancel()
            observeProjectJob =
                viewModelScope.launch {
                    getPairsByProjectUseCase(projectId).collect { pairs ->
                        _beforePreviewUris.value = pairs.map { it.beforePhotoUri }
                    }
                }
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
