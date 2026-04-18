package com.pairshot.feature.camera.ui.viewmodel

import android.content.Context
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.extensions.ExtensionsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.core.domain.capture.SaveBeforePhotoUseCase
import com.pairshot.core.domain.pair.GetPairsByProjectUseCase
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.feature.camera.ui.component.ZoomStateHolder
import com.pairshot.feature.camera.ui.component.ZoomUiState
import com.pairshot.feature.camera.ui.sensor.LevelSensorManager
import com.pairshot.feature.camera.ui.state.CameraCapabilities
import com.pairshot.feature.camera.ui.state.CameraSettingsState
import com.pairshot.feature.camera.ui.state.CameraSettingsStateHolder
import com.pairshot.feature.camera.ui.state.FlashMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
        @ApplicationContext context: Context,
        private val saveBeforePhotoUseCase: SaveBeforePhotoUseCase,
        private val getPairsByProjectUseCase: GetPairsByProjectUseCase,
        private val appSettingsRepository: AppSettingsRepository,
    ) : ViewModel() {
        private val _events = MutableSharedFlow<CameraEvent>()
        val events: SharedFlow<CameraEvent> = _events.asSharedFlow()

        private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
        val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

        private val zoomHolder = ZoomStateHolder()
        val zoomUiState: StateFlow<ZoomUiState> = zoomHolder.zoomUiState

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        private val _beforePreviewUris = MutableStateFlow<List<String>>(emptyList())
        val beforePreviewUris: StateFlow<List<String>> = _beforePreviewUris.asStateFlow()

        private val settingsHolder =
            CameraSettingsStateHolder(
                runBlocking {
                    val s = appSettingsRepository.settingsFlow.first()
                    CameraSettingsState(
                        gridEnabled = s.cameraGridEnabled,
                        levelEnabled = s.cameraLevelEnabled,
                        flashMode = FlashMode.valueOf(s.cameraFlashMode),
                        nightModeEnabled = s.cameraNightModeEnabled,
                        hdrEnabled = s.cameraHdrEnabled,
                    )
                },
            )
        val capabilities: StateFlow<CameraCapabilities> = settingsHolder.capabilities
        val settingsState: StateFlow<CameraSettingsState> = settingsHolder.settingsState

        val levelSensorManager: LevelSensorManager = LevelSensorManager(context)

        init {
            if (settingsHolder.settingsState.value.levelEnabled) {
                levelSensorManager.start()
            }
        }

        private var observedProjectId: Long? = null
        private var observeProjectJob: Job? = null

        val imageCapture: ImageCapture =
            ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

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

        fun onShutterClick(
            projectId: Long,
            tempFileUri: String,
        ) {
            if (_isSaving.value) return
            viewModelScope.launch {
                _isSaving.value = true
                try {
                    val pairId =
                        saveBeforePhotoUseCase(
                            projectId = projectId,
                            tempFileUri = tempFileUri,
                            zoomLevel = zoomHolder.zoomUiState.value.currentRatio,
                            lensId = null,
                        )
                    _events.emit(CameraEvent.PhotoSaved(pairId))
                } catch (e: Exception) {
                    _events.emit(CameraEvent.SaveError(e.message ?: "저장에 실패했습니다."))
                } finally {
                    try {
                        val path = java.net.URI(tempFileUri).path
                        if (path != null) java.io.File(path).delete()
                    } catch (_: Exception) {
                    }
                    _isSaving.value = false
                }
            }
        }

        fun emitCaptureError(message: String) {
            viewModelScope.launch {
                _events.emit(CameraEvent.CaptureError(message))
            }
        }

        fun toggleLensFacing() {
            _lensFacing.value =
                if (_lensFacing.value == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
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
        }
    }
