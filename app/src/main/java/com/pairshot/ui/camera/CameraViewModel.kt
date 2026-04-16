package com.pairshot.ui.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.domain.usecase.capture.SaveBeforePhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    ) : ViewModel() {
        private val _events = MutableSharedFlow<CameraEvent>()
        val events: SharedFlow<CameraEvent> = _events.asSharedFlow()

        private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
        val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

        private val _zoomRatio = MutableStateFlow(1f)
        val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        // CameraScreen에서 bindToLifecycle 시 함께 바인딩
        val imageCapture: ImageCapture =
            ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

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
                            zoomLevel = _zoomRatio.value,
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

        fun updateZoomRatio(ratio: Float) {
            _zoomRatio.value = ratio
        }
    }
