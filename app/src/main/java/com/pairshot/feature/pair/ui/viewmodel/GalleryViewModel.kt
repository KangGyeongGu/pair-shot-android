package com.pairshot.feature.pair.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.app.navigation.route.ProjectDetail
import com.pairshot.feature.pair.domain.model.PairStatus
import com.pairshot.feature.pair.domain.model.PhotoPair
import com.pairshot.feature.pair.domain.repository.PhotoPairRepository
import com.pairshot.feature.pair.domain.usecase.BatchCombineUseCase
import com.pairshot.feature.pair.domain.usecase.GetPairsByProjectUseCase
import com.pairshot.feature.project.domain.repository.ProjectRepository
import com.pairshot.feature.project.domain.usecase.DeleteProjectUseCase
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
        private val deleteProjectUseCase: DeleteProjectUseCase,
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

        private val _projectDeletedEvent = MutableSharedFlow<Unit>()
        val projectDeletedEvent: SharedFlow<Unit> = _projectDeletedEvent.asSharedFlow()

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
                        val afterUris =
                            pairs
                                .filter { it.afterPhotoUri != null }
                                .mapNotNull { it.afterPhotoUri }
                        if (afterUris.isNotEmpty()) {
                            val existingUris = photoPairRepository.checkUrisExist(afterUris)
                            val stalePairs =
                                pairs.filter { pair ->
                                    pair.afterPhotoUri != null && pair.afterPhotoUri !in existingUris
                                }
                            if (stalePairs.isNotEmpty()) {
                                stalePairs.forEach { photoPairRepository.resetAfterPhoto(it.id) }
                                return@collect
                            }
                        }

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
                        ids.forEach { id ->
                            try {
                                photoPairRepository.removeCombinedPhoto(id)
                            } catch (_: Exception) {
                            }
                        }
                        _snackbarMessage.emit("${ids.size}개 합성 이미지 삭제")
                    } else {
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

        fun renameProject(newName: String) {
            viewModelScope.launch {
                projectRepository.getById(projectId)?.let { project ->
                    projectRepository.update(
                        project.copy(name = newName, updatedAt = System.currentTimeMillis()),
                    )
                    _uiState.update { state ->
                        if (state is GalleryUiState.Success) {
                            state.copy(projectName = newName)
                        } else {
                            state
                        }
                    }
                }
            }
        }

        fun deleteProject() {
            viewModelScope.launch {
                projectRepository.getById(projectId)?.let { project ->
                    deleteProjectUseCase(project)
                    _projectDeletedEvent.emit(Unit)
                }
            }
        }
    }
