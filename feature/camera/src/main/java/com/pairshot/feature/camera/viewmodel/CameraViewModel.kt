package com.pairshot.feature.camera.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.core.domain.pair.GetLatestBeforeThumbnailUseCase
import com.pairshot.core.domain.pair.PhotoPairRepository
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.model.CameraCapabilities
import com.pairshot.core.model.FlashMode
import com.pairshot.core.model.LensFacing
import com.pairshot.core.navigation.Camera
import com.pairshot.feature.camera.component.ZoomStateHolder
import com.pairshot.feature.camera.component.ZoomUiState
import com.pairshot.feature.camera.state.CameraSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

data class InitialCameraSessionConfig(
    val flashMode: FlashMode,
    val nightModeEnabled: Boolean,
    val hdrEnabled: Boolean,
)

data class CapabilityAdjustment(
    val flashMode: FlashMode? = null,
    val nightModeEnabled: Boolean? = null,
    val hdrEnabled: Boolean? = null,
)

@HiltViewModel
class CameraViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val photoPairRepository: PhotoPairRepository,
        private val getLatestBeforeThumbnailUseCase: GetLatestBeforeThumbnailUseCase,
        private val appSettingsRepository: AppSettingsRepository,
    ) : ViewModel() {
        private val albumId: Long? = savedStateHandle.toRoute<Camera>().albumId
        private val sessionStartTimestamp: Long = System.currentTimeMillis()

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

        val lastPairThumbnailUri: StateFlow<String?> =
            getLatestBeforeThumbnailUseCase()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        private val _settingsState = MutableStateFlow(CameraSettingsState())
        val settingsState: StateFlow<CameraSettingsState> = _settingsState.asStateFlow()

        private var observePairsJob: Job? = null

        suspend fun loadInitialSettings(): InitialCameraSessionConfig {
            val s = appSettingsRepository.getCurrent()
            val initial =
                CameraSettingsState(
                    gridEnabled = s.cameraGridEnabled,
                    levelEnabled = s.cameraLevelEnabled,
                    flashMode = runCatching { FlashMode.valueOf(s.cameraFlashMode) }.getOrDefault(FlashMode.OFF),
                    nightModeEnabled = s.cameraNightModeEnabled,
                    hdrEnabled = s.cameraHdrEnabled,
                )
            _settingsState.value = initial
            return InitialCameraSessionConfig(initial.flashMode, initial.nightModeEnabled, initial.hdrEnabled)
        }

        fun onCameraZoomCapabilities(
            min: Float,
            max: Float,
        ) {
            zoomHolder.initFromZoomState(min, max)
        }

        fun updateZoomRatio(ratio: Float) {
            zoomHolder.updateZoomRatio(ratio)
        }

        fun onPresetTapped(preset: Float) {
            zoomHolder.onPresetTapped(preset)
        }

        fun applyCustomRatio() {
            zoomHolder.applyCustomRatio()
        }

        fun resetZoomForLensSwitch() {
            zoomHolder.resetZoomForLensSwitch()
        }

        fun adjustForCapabilities(caps: CameraCapabilities): CapabilityAdjustment {
            val state = _settingsState.value
            var changed = state
            var adjustedFlash: FlashMode? = null
            var adjustedNight: Boolean? = null
            var adjustedHdr: Boolean? = null
            if (!caps.hasFlash && state.flashMode != FlashMode.OFF) {
                changed = changed.copy(flashMode = FlashMode.OFF)
                adjustedFlash = FlashMode.OFF
            }
            if (!caps.nightModeAvailable && state.nightModeEnabled) {
                changed = changed.copy(nightModeEnabled = false)
                adjustedNight = false
            }
            if (!caps.hdrAvailable && state.hdrEnabled) {
                changed = changed.copy(hdrEnabled = false)
                adjustedHdr = false
            }
            if (changed !== state) _settingsState.value = changed
            return CapabilityAdjustment(adjustedFlash, adjustedNight, adjustedHdr)
        }

        fun startCapturing() {
            _isSaving.value = true
        }

        fun finishCapturing() {
            _isSaving.value = false
        }

        fun emitCaptureError(message: String) {
            viewModelScope.launch {
                _events.emit(CameraEvent.CaptureError(message))
            }
        }

        fun saveBeforePhoto(
            tempUri: String,
            zoomLevel: Float,
        ) {
            viewModelScope.launch {
                try {
                    val pairId =
                        photoPairRepository.saveBeforePhoto(
                            tempFileUri = tempUri,
                            zoomLevel = zoomLevel,
                            albumId = albumId,
                        )
                    _events.emit(CameraEvent.PhotoSaved(pairId))
                } catch (e: Exception) {
                    _events.emit(CameraEvent.SaveError(e.message ?: "저장에 실패했습니다."))
                } finally {
                    _isSaving.value = false
                }
            }
        }

        fun toggleLensFacing(): LensFacing {
            val next = if (_lensFacing.value == LensFacing.BACK) LensFacing.FRONT else LensFacing.BACK
            _lensFacing.value = next
            zoomHolder.resetZoomForLensSwitch()
            return next
        }

        fun observeBeforeUris() {
            if (observePairsJob?.isActive == true) return
            observePairsJob =
                viewModelScope.launch {
                    photoPairRepository.observeAll().collect { pairs ->
                        _beforePreviewUris.value =
                            pairs
                                .filter { it.beforeTimestamp >= sessionStartTimestamp }
                                .map { it.beforePhotoUri }
                    }
                }
        }

        fun toggleGrid() {
            _settingsState.update { it.copy(gridEnabled = !it.gridEnabled) }
            persistSettings()
        }

        fun toggleLevel() {
            _settingsState.update { it.copy(levelEnabled = !it.levelEnabled) }
            persistSettings()
        }

        fun cycleFlash(): FlashMode {
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
            persistSettings()
            return _settingsState.value.flashMode
        }

        fun toggleNightMode(): Boolean {
            val next = !_settingsState.value.nightModeEnabled
            _settingsState.update {
                it.copy(
                    nightModeEnabled = next,
                    hdrEnabled = if (next) false else it.hdrEnabled,
                )
            }
            persistSettings()
            return next
        }

        fun toggleHdr(): Boolean {
            val next = !_settingsState.value.hdrEnabled
            _settingsState.update {
                it.copy(
                    hdrEnabled = next,
                    nightModeEnabled = if (next) false else it.nightModeEnabled,
                )
            }
            persistSettings()
            return next
        }

        fun setExposureIndex(index: Int) {
            _settingsState.update { it.copy(exposureIndex = index) }
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
    }
