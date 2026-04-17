package com.pairshot.ui.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.domain.usecase.capture.SaveBeforePhotoUseCase
import com.pairshot.domain.usecase.pair.GetPairsByProjectUseCase
import com.pairshot.ui.component.ZoomStateHolder
import com.pairshot.ui.component.ZoomUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    }
