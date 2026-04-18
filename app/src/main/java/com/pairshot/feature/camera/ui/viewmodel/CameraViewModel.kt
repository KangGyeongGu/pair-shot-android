package com.pairshot.feature.camera.ui.viewmodel

import android.content.Context
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.extensions.ExtensionsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.domain.usecase.capture.SaveBeforePhotoUseCase
import com.pairshot.domain.usecase.pair.GetPairsByProjectUseCase
import com.pairshot.feature.camera.ui.component.ZoomStateHolder
import com.pairshot.feature.camera.ui.component.ZoomUiState
import com.pairshot.feature.camera.ui.sensor.LevelSensorManager
import com.pairshot.feature.camera.ui.state.CameraCapabilities
import com.pairshot.feature.camera.ui.state.CameraSettingsState
import com.pairshot.feature.camera.ui.state.CameraSettingsStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

@HiltViewModel
class CameraViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        private val saveBeforePhotoUseCase: SaveBeforePhotoUseCase,
        private val getPairsByProjectUseCase: GetPairsByProjectUseCase,
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

        // 카메라 설정 상태 — CameraSettingsStateHolder로 위임
        private val settingsHolder = CameraSettingsStateHolder()
        val capabilities: StateFlow<CameraCapabilities> = settingsHolder.capabilities
        val settingsState: StateFlow<CameraSettingsState> = settingsHolder.settingsState

        // 수평계 센서
        val levelSensorManager: LevelSensorManager = LevelSensorManager(context)

        private var observedProjectId: Long? = null
        private var observeProjectJob: Job? = null

        // CameraScreen에서 bindToLifecycle 시 함께 바인딩
        val imageCapture: ImageCapture =
            ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

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
                    // 임시 파일 cleanup (file:// URI → path 추출)
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

        // --- 카메라 설정 ---

        /**
         * 카메라 바인딩 완료 후 CameraInfo와 ExtensionsManager를 전달해 capabilities를 갱신한다.
         * 렌즈 전환 시에도 재호출해야 한다.
         */
        fun updateCapabilities(
            cameraInfo: CameraInfo,
            extensionsManager: ExtensionsManager,
        ) {
            settingsHolder.updateCapabilities(cameraInfo, extensionsManager, _lensFacing.value)
        }

        fun toggleGrid() {
            settingsHolder.toggleGrid()
        }

        /**
         * 수평계 ON/OFF. 활성화 시 센서 리스너를 등록하고 비활성화 시 해제한다.
         */
        fun toggleLevel() {
            val active = settingsHolder.toggleLevel()
            if (active) levelSensorManager.start() else levelSensorManager.stop()
        }

        /** OFF → AUTO → ON → TORCH → OFF 순환 */
        fun cycleFlash() {
            settingsHolder.cycleFlash()
        }

        /**
         * 야간모드 토글. HDR과 동시 활성화 불가 — 야간모드를 켜면 HDR은 자동 OFF.
         * Extension 재바인딩은 Screen에서 settingsState를 collect해 처리한다.
         */
        fun toggleNightMode() {
            settingsHolder.toggleNightMode()
        }

        /**
         * HDR 토글. 야간모드와 동시 활성화 불가 — HDR을 켜면 야간모드는 자동 OFF.
         * Extension 재바인딩은 Screen에서 settingsState를 collect해 처리한다.
         */
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

        /**
         * 현재 활성화된 Extension에 따라 적절한 CameraSelector를 반환한다.
         * 야간모드/HDR 비활성 시에는 기본 selector를 반환한다.
         */
        fun getExtensionCameraSelector(extensionsManager: ExtensionsManager): CameraSelector =
            settingsHolder.getExtensionCameraSelector(extensionsManager, _lensFacing.value)

        /**
         * 현재 flashMode에 따라 ImageCapture의 flashMode를 설정한다.
         * TORCH는 CameraControl.enableTorch(true)로 처리하므로 Screen에서 별도 처리 필요.
         */
        fun applyFlashMode(imageCapture: ImageCapture) {
            settingsHolder.applyFlashMode(imageCapture)
        }

        override fun onCleared() {
            super.onCleared()
            levelSensorManager.stop()
        }
    }
