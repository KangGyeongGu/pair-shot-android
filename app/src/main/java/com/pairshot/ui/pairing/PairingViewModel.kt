package com.pairshot.ui.pairing

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.domain.model.PhotoPair
import com.pairshot.domain.usecase.capture.SaveAfterPhotoUseCase
import com.pairshot.domain.usecase.pair.GetUnpairedPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PairingEvent {
    data object AllCompleted : PairingEvent

    data class AfterSaved(
        val pairId: Long,
    ) : PairingEvent

    data class CaptureError(
        val message: String,
    ) : PairingEvent

    data class SaveError(
        val message: String,
    ) : PairingEvent
}

@HiltViewModel
class PairingViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        getUnpairedPhotosUseCase: GetUnpairedPhotosUseCase,
        private val saveAfterPhotoUseCase: SaveAfterPhotoUseCase,
    ) : ViewModel() {
        private val projectId: Long = savedStateHandle["projectId"] ?: 0L
        private val initialPairId: Long? = savedStateHandle["initialPairId"]

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

        private val _zoomRatio = MutableStateFlow(1f)
        val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        // 오버레이 투명도 (0f ~ 0.5f)
        private val _overlayAlpha = MutableStateFlow(0.3f)
        val overlayAlpha: StateFlow<Float> = _overlayAlpha.asStateFlow()

        // 이벤트
        private val _events = MutableSharedFlow<PairingEvent>()
        val events: SharedFlow<PairingEvent> = _events.asSharedFlow()

        // CameraScreen에서 bindToLifecycle 시 함께 바인딩
        val imageCapture: ImageCapture =
            ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

        // 초기 인덱스 설정 (initialPairId가 있으면 해당 pair로 이동)
        private var initialIndexSet = false

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
                    _events.emit(PairingEvent.AfterSaved(currentPair.id))
                    // 촬영 완료 후 → unpairedPhotos Flow 자동 갱신
                    // 인덱스 범위 보정은 onUnpairedPhotosUpdated에서 처리됨
                } catch (e: Exception) {
                    _events.emit(PairingEvent.SaveError(e.message ?: "저장에 실패했습니다."))
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
                _events.emit(PairingEvent.CaptureError(message))
            }
        }

        fun emitAllCompleted() {
            viewModelScope.launch {
                _events.emit(PairingEvent.AllCompleted)
            }
        }

        fun updateZoomRatio(ratio: Float) {
            _zoomRatio.value = ratio
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
    }
