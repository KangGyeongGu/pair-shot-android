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
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.navigation.AfterCamera
import com.pairshot.feature.camera.component.ZoomStateHolder
import com.pairshot.feature.camera.component.ZoomUiState
import com.pairshot.feature.camera.state.CameraSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

data class OverlayInputs(
    val pair: PhotoPair?,
    val enabled: Boolean,
    val alpha: Float,
    val lensFacing: LensFacing,
)

@HiltViewModel
class AfterCameraViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val photoPairRepository: PhotoPairRepository,
        private val getLatestBeforeThumbnailUseCase: GetLatestBeforeThumbnailUseCase,
        private val appSettingsRepository: AppSettingsRepository,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<AfterCamera>()
        private val initialPairId: Long? = route.initialPairId
        private val albumId: Long? = route.albumId

        val totalPairCount: StateFlow<Int> =
            photoPairRepository
                .countAll()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

        val unpairedPhotos: StateFlow<List<PhotoPair>> =
            (
                if (albumId != null) {
                    photoPairRepository.observeUnpairedByAlbum(albumId)
                } else {
                    photoPairRepository.observeUnpaired()
                }
            ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        val lastPairThumbnailUri: StateFlow<String?> =
            getLatestBeforeThumbnailUseCase()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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

        private val _overlayAlpha = MutableStateFlow(0.35f)
        val overlayAlpha: StateFlow<Float> = _overlayAlpha.asStateFlow()

        val overlayInputs: StateFlow<OverlayInputs> =
            combine(
                unpairedPhotos,
                _currentIndex,
                _overlayEnabled,
                _overlayAlpha,
                _lensFacing,
            ) { photos, index, enabled, alpha, lens ->
                OverlayInputs(
                    pair = photos.getOrNull(index),
                    enabled = enabled,
                    alpha = alpha,
                    lensFacing = lens,
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                OverlayInputs(null, true, 0.35f, LensFacing.BACK),
            )

        private val _settingsState = MutableStateFlow(CameraSettingsState())
        val settingsState: StateFlow<CameraSettingsState> = _settingsState.asStateFlow()

        private val _events = MutableSharedFlow<AfterCameraEvent>()
        val events: SharedFlow<AfterCameraEvent> = _events.asSharedFlow()

        private val _pairsLoaded = MutableStateFlow(false)
        val pairsLoaded: StateFlow<Boolean> = _pairsLoaded.asStateFlow()

        private var initialIndexSet = false

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
            _overlayEnabled.value = s.overlayEnabled
            _overlayAlpha.value = s.defaultOverlayAlpha.coerceIn(0f, 1f)
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

        fun restoreZoomForPair(zoomLevel: Float?) {
            zoomHolder.restoreZoomForPair(zoomLevel)
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

        fun onUnpairedPhotosUpdated(photos: List<PhotoPair>) {
            _pairsLoaded.value = true
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

        fun startCapturing() {
            _isSaving.value = true
        }

        fun finishCapturing() {
            _isSaving.value = false
        }

        fun emitCaptureError(message: String) {
            viewModelScope.launch {
                _events.emit(AfterCameraEvent.CaptureError(message))
            }
        }

        fun saveAfterPhoto(tempUri: String) {
            val photos = unpairedPhotos.value
            if (photos.isEmpty()) {
                _isSaving.value = false
                return
            }
            val currentPair = photos.getOrNull(_currentIndex.value)
            if (currentPair == null) {
                _isSaving.value = false
                return
            }
            viewModelScope.launch {
                try {
                    photoPairRepository.saveAfterPhoto(
                        pairId = currentPair.id,
                        tempFileUri = tempUri,
                    )
                    _events.emit(AfterCameraEvent.AfterSaved(currentPair.id))
                } catch (e: Exception) {
                    _events.emit(AfterCameraEvent.SaveError(e.message ?: "저장에 실패했습니다."))
                } finally {
                    _isSaving.value = false
                }
            }
        }

        fun emitAllCompleted() {
            viewModelScope.launch {
                _events.emit(AfterCameraEvent.AllCompleted)
            }
        }

        fun toggleOverlay() {
            _overlayEnabled.value = !_overlayEnabled.value
        }

        fun updateOverlayAlpha(alpha: Float) {
            _overlayAlpha.value = alpha.coerceIn(0f, 1f)
        }

        fun toggleLensFacing(): LensFacing {
            val next = if (_lensFacing.value == LensFacing.BACK) LensFacing.FRONT else LensFacing.BACK
            _lensFacing.value = next
            zoomHolder.resetZoomForLensSwitch()
            return next
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
