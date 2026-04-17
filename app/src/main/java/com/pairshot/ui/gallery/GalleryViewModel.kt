package com.pairshot.ui.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.domain.model.PairStatus
import com.pairshot.domain.model.PhotoPair
import com.pairshot.domain.repository.PhotoPairRepository
import com.pairshot.domain.repository.ProjectRepository
import com.pairshot.domain.usecase.combine.BatchCombineUseCase
import com.pairshot.domain.usecase.pair.GetPairsByProjectUseCase
import com.pairshot.ui.navigation.ProjectDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GalleryUiState {
    data object Loading : GalleryUiState

    data class Success(
        val projectName: String,
        val pairs: List<PhotoPair>,
        val unpairedCount: Int,
        val combinedCount: Int,
    ) : GalleryUiState

    data class Error(
        val message: String,
    ) : GalleryUiState
}

data class CombineProgress(
    val current: Int,
    val total: Int,
)

@HiltViewModel
class GalleryViewModel
    @Inject
    constructor(
        private val getPairsUseCase: GetPairsByProjectUseCase,
        private val projectRepository: ProjectRepository,
        private val batchCombineUseCase: BatchCombineUseCase,
        private val photoPairRepository: PhotoPairRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val projectId: Long = savedStateHandle.toRoute<ProjectDetail>().projectId

        private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
        val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

        private val _showCombinedOnly = MutableStateFlow(false)
        val showCombinedOnly: StateFlow<Boolean> = _showCombinedOnly.asStateFlow()

        private val _selectionMode = MutableStateFlow(false)
        val selectionMode: StateFlow<Boolean> = _selectionMode.asStateFlow()

        private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
        val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

        private val _combineProgress = MutableStateFlow<CombineProgress?>(null)
        val combineProgress: StateFlow<CombineProgress?> = _combineProgress.asStateFlow()

        private val _snackbarMessage = MutableSharedFlow<String>()
        val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

        init {
            loadPairs()
        }

        private fun loadPairs() {
            viewModelScope.launch {
                val projectName =
                    try {
                        projectRepository.getById(projectId)?.name ?: "프로젝트"
                    } catch (_: Exception) {
                        "프로젝트"
                    }

                getPairsUseCase(projectId)
                    .catch { e -> _uiState.value = GalleryUiState.Error(e.message ?: "알 수 없는 오류") }
                    .collect { pairs ->
                        _uiState.value =
                            GalleryUiState.Success(
                                projectName = projectName,
                                pairs = pairs,
                                unpairedCount = pairs.count { it.status == PairStatus.BEFORE_ONLY },
                                combinedCount = pairs.count { it.status == PairStatus.COMBINED },
                            )
                    }
            }
        }

        fun toggleFilter() {
            _showCombinedOnly.update { !it }
        }

        fun enterSelectionMode() {
            _selectionMode.value = true
        }

        fun exitSelectionMode() {
            _selectionMode.value = false
            _selectedIds.value = emptySet()
        }

        fun toggleSelection(pairId: Long) {
            _selectedIds.update { ids ->
                if (pairId in ids) ids - pairId else ids + pairId
            }
        }

        fun selectAll() {
            val state = _uiState.value
            if (state is GalleryUiState.Success) {
                _selectedIds.value = state.pairs.map { it.id }.toSet()
            }
        }

        fun longPressSelect(pairId: Long) {
            if (!_selectionMode.value) {
                _selectionMode.value = true
            }
            _selectedIds.update { it + pairId }
        }

        fun combineSelected() {
            viewModelScope.launch {
                val ids = _selectedIds.value.toList()
                val pairedIds =
                    ids.filter { id ->
                        val state = _uiState.value
                        if (state is GalleryUiState.Success) {
                            state.pairs.find { it.id == id }?.status == PairStatus.PAIRED
                        } else {
                            false
                        }
                    }
                if (pairedIds.isEmpty()) {
                    _snackbarMessage.emit("합성할 수 있는 페어가 없습니다")
                    return@launch
                }
                _combineProgress.value = CombineProgress(0, pairedIds.size)
                val success =
                    batchCombineUseCase(pairedIds) { current, total ->
                        _combineProgress.value = CombineProgress(current, total)
                    }
                _combineProgress.value = null
                _snackbarMessage.emit("${success}개 합성 완료")
                exitSelectionMode()
            }
        }

        fun deleteSelected() {
            viewModelScope.launch {
                val ids = _selectedIds.value.toList()
                val state = _uiState.value
                if (state is GalleryUiState.Success) {
                    if (_showCombinedOnly.value) {
                        // 합성 탭: 합성 이미지만 제거, 페어는 PAIRED로 유지
                        ids.forEach { id ->
                            try {
                                photoPairRepository.removeCombinedPhoto(id)
                            } catch (_: Exception) {
                            }
                        }
                        _snackbarMessage.emit("${ids.size}개 합성 이미지 삭제")
                    } else {
                        // 전체 탭: 페어 전체 삭제
                        ids.forEach { id ->
                            state.pairs.find { it.id == id }?.let { pair ->
                                photoPairRepository.delete(pair)
                            }
                        }
                        _snackbarMessage.emit("${ids.size}개 삭제 완료")
                    }
                }
                exitSelectionMode()
            }
        }
    }
