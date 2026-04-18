package com.pairshot.feature.camera.ui.viewmodel

import android.content.Context
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.extensions.ExtensionsManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.domain.model.PhotoPair
import com.pairshot.domain.repository.AppSettingsRepository
import com.pairshot.domain.usecase.capture.SaveAfterPhotoUseCase
import com.pairshot.domain.usecase.pair.GetPairsByProjectUseCase
import com.pairshot.domain.usecase.pair.GetUnpairedPhotosUseCase
import com.pairshot.feature.camera.ui.component.ZoomStateHolder
import com.pairshot.feature.camera.ui.component.ZoomUiState
import com.pairshot.feature.camera.ui.sensor.LevelSensorManager
import com.pairshot.feature.camera.ui.state.CameraCapabilities
import com.pairshot.feature.camera.ui.state.CameraSettingsState
import com.pairshot.feature.camera.ui.state.CameraSettingsStateHolder
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

        // 미완료 Before 목록 (Flow로 실시간 반영)
        val unpairedPhotos: StateFlow<List<PhotoPair>> =
            getUnpairedPhotosUseCase(projectId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        // 현재 선택된 인덱스
        private val _currentIndex = MutableStateFlow(0)
        val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

        // 카메라 상태
        private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
        val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

        private val zoomHolder = ZoomStateHolder()
        val zoomUiState: StateFlow<ZoomUiState> = zoomHolder.zoomUiState

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        // 오버레이 활성화 여부 — 설정에서 읽어옴
        private val _overlayEnabled = MutableStateFlow(true)
        val overlayEnabled: StateFlow<Boolean> = _overlayEnabled.asStateFlow()

        // 오버레이 투명도 (0f ~ 0.5f) — 초기값은 설정에서 읽어옴, 세션 내 변경은 임시 적용
        private val _overlayAlpha = MutableStateFlow(0.3f)
        val overlayAlpha: StateFlow<Float> = _overlayAlpha.asStateFlow()

        init {
            viewModelScope.launch {
                val settings = appSettingsRepository.settingsFlow.first()
                _overlayEnabled.value = settings.overlayEnabled
                _overlayAlpha.value = settings.defaultOverlayAlpha.coerceIn(0f, 0.5f)
            }
        }

        // 카메라 설정 상태 — CameraSettingsStateHolder로 위임
        private val settingsHolder = CameraSettingsStateHolder()
        val capabilities: StateFlow<CameraCapabilities> = settingsHolder.capabilities
        val settingsState: StateFlow<CameraSettingsState> = settingsHolder.settingsState

        // 수평계 센서
        val levelSensorManager: LevelSensorManager = LevelSensorManager(context)

        // 이벤트
        private val _events = MutableSharedFlow<AfterCameraEvent>()
        val events: SharedFlow<AfterCameraEvent> = _events.asSharedFlow()

        // CameraScreen에서 bindToLifecycle 시 함께 바인딩
        val imageCapture: ImageCapture =
            ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

        // 초기 인덱스 설정 (initialPairId가 있으면 해당 pair로 이동)
        private var initialIndexSet = false

        /**
         * Screen에서 cameraInfo.zoomState를 observe해 최초 non-null 값을 이 함수에 전달한다.
         * min/max만 받아 Android 의존성을 ViewModel에서 제거한다.
         */
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
            // 인덱스가 범위를 벗어나면 보정
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
                    // 촬영 완료 후 → unpairedPhotos Flow 자동 갱신
                    // 인덱스 범위 보정은 onUnpairedPhotosUpdated에서 처리됨
                } catch (e: Exception) {
                    _events.emit(AfterCameraEvent.SaveError(e.message ?: "저장에 실패했습니다."))
                } finally {
                    try {
                        val path = java.net.URI(tempFileUri).path
                        if (path != null) java.io.File(path).delete()
                    } catch (_: Exception) {
                        // cleanup 실패는 무시
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

        // --- 카메라 설정 ---

        fun updateCapabilities(
            cameraInfo: CameraInfo,
            extensionsManager: ExtensionsManager,
        ) {
            settingsHolder.updateCapabilities(cameraInfo, extensionsManager, _lensFacing.value)
        }

        fun toggleGrid() {
            settingsHolder.toggleGrid()
        }

        fun toggleLevel() {
            val active = settingsHolder.toggleLevel()
            if (active) levelSensorManager.start() else levelSensorManager.stop()
        }

        fun cycleFlash() {
            settingsHolder.cycleFlash()
        }

        fun toggleNightMode() {
            settingsHolder.toggleNightMode()
        }

        fun toggleHdr() {
            settingsHolder.toggleHdr()
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

        override fun onCleared() {
            super.onCleared()
            levelSensorManager.stop()
        }
    }
