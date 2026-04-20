package com.pairshot.feature.camera.ui.viewmodel

import android.content.Context
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.extensions.ExtensionsManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.core.domain.capture.SaveAfterPhotoUseCase
import com.pairshot.core.domain.pair.GetPairsByProjectUseCase
import com.pairshot.core.domain.pair.GetUnpairedPhotosUseCase
import com.pairshot.core.domain.pair.PhotoPair
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.feature.camera.ui.component.ZoomStateHolder
import com.pairshot.feature.camera.ui.component.ZoomUiState
import com.pairshot.feature.camera.ui.sensor.CaptureOrientationManager
import com.pairshot.feature.camera.ui.sensor.LevelSensorManager
import com.pairshot.feature.camera.ui.state.CameraCapabilities
import com.pairshot.feature.camera.ui.state.CameraSettingsState
import com.pairshot.feature.camera.ui.state.CameraSettingsStateHolder
import com.pairshot.feature.camera.ui.state.FlashMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
        @ApplicationContext context: Context,
        savedStateHandle: SavedStateHandle,
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

        private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
        val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

        private val zoomHolder = ZoomStateHolder()
        val zoomUiState: StateFlow<ZoomUiState> = zoomHolder.zoomUiState

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        private val _overlayEnabled = MutableStateFlow(true)
        val overlayEnabled: StateFlow<Boolean> = _overlayEnabled.asStateFlow()

        private val _overlayAlpha = MutableStateFlow(0.3f)
        val overlayAlpha: StateFlow<Float> = _overlayAlpha.asStateFlow()

        private val settingsHolder = CameraSettingsStateHolder()
        val capabilities: StateFlow<CameraCapabilities> = settingsHolder.capabilities
        val settingsState: StateFlow<CameraSettingsState> = settingsHolder.settingsState

        val imageCapture: ImageCapture =
            ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

        val levelSensorManager: LevelSensorManager = LevelSensorManager(context)
        private val captureOrientationManager = CaptureOrientationManager(context, imageCapture)

        init {
            captureOrientationManager.start()
            viewModelScope.launch {
                val s = appSettingsRepository.settingsFlow.first()
                settingsHolder.applyPersistedSettings(
                    CameraSettingsState(
                        gridEnabled = s.cameraGridEnabled,
                        levelEnabled = s.cameraLevelEnabled,
                        flashMode = FlashMode.valueOf(s.cameraFlashMode),
                        nightModeEnabled = s.cameraNightModeEnabled,
                        hdrEnabled = s.cameraHdrEnabled,
                    ),
                )
                if (settingsHolder.settingsState.value.levelEnabled) {
                    levelSensorManager.start()
                }
                _overlayEnabled.value = s.overlayEnabled
                _overlayAlpha.value = s.defaultOverlayAlpha.coerceIn(0f, 0.5f)
            }
            if (settingsHolder.settingsState.value.levelEnabled) {
                levelSensorManager.start()
            }
        }

        private val _events = MutableSharedFlow<AfterCameraEvent>()
        val events: SharedFlow<AfterCameraEvent> = _events.asSharedFlow()

        private var initialIndexSet = false

        fun initFromZoomState(
            minRatio: Float,
            maxRatio: Float,
        ) {
            zoomHolder.initFromZoomState(minRatio, maxRatio)
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

        fun onAfterCaptured(tempFileUri: String) {
            val photos = unpairedPhotos.value
            if (photos.isEmpty() || _isSaving.value) return
            val currentPair = photos.getOrNull(_currentIndex.value) ?: return

            viewModelScope.launch {
                _isSaving.value = true
                try {
                    saveAfterPhotoUseCase(
                        pairId = currentPair.id,
                        tempFileUri = tempFileUri,
                    )
                    _events.emit(AfterCameraEvent.AfterSaved(currentPair.id))
                } catch (e: Exception) {
                    _events.emit(AfterCameraEvent.SaveError(e.message ?: "저장에 실패했습니다."))
                } finally {
                    try {
                        val path = java.net.URI(tempFileUri).path
                        if (path != null) java.io.File(path).delete()
                    } catch (e: Exception) {
                        Timber.d(e, "임시 파일 삭제 실패: $tempFileUri")
                    }
                    _isSaving.value = false
                }
            }
        }

        fun emitCaptureError(message: String) {
            viewModelScope.launch {
                _events.emit(AfterCameraEvent.CaptureError(message))
            }
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
            _overlayAlpha.value = alpha.coerceIn(0f, 0.5f)
        }

        fun toggleLensFacing() {
            _lensFacing.value =
                if (_lensFacing.value == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
        }

        fun updateCapabilities(
            cameraInfo: CameraInfo,
            extensionsManager: ExtensionsManager,
        ) {
            settingsHolder.updateCapabilities(cameraInfo, extensionsManager, _lensFacing.value)
        }

        fun toggleGrid() {
            settingsHolder.toggleGrid()
            persistSettings()
        }

        fun toggleLevel() {
            val active = settingsHolder.toggleLevel()
            if (active) levelSensorManager.start() else levelSensorManager.stop()
            persistSettings()
        }

        fun cycleFlash() {
            settingsHolder.cycleFlash()
            persistSettings()
        }

        fun toggleNightMode() {
            settingsHolder.toggleNightMode()
            persistSettings()
        }

        fun toggleHdr() {
            settingsHolder.toggleHdr()
            persistSettings()
        }

        fun setExposureIndex(index: Int) {
            settingsHolder.setExposureIndex(index)
        }

        fun toggleSettingsPanel() {
            settingsHolder.toggleSettingsPanel()
        }

        fun dismissSettingsPanel() {
            settingsHolder.dismissSettingsPanel()
        }

        fun getExtensionCameraSelector(extensionsManager: ExtensionsManager): CameraSelector =
            settingsHolder.getExtensionCameraSelector(extensionsManager, _lensFacing.value)

        fun applyFlashMode(imageCapture: ImageCapture) {
            settingsHolder.applyFlashMode(imageCapture)
        }

        private fun persistSettings() {
            val state = settingsHolder.settingsState.value
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
            levelSensorManager.stop()
            captureOrientationManager.stop()
        }
    }
