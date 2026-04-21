package com.pairshot.feature.pair.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.core.navigation.ProjectDetail
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.domain.combine.CombineSettingsRepository
import com.pairshot.core.domain.pair.BatchCombineResult
import com.pairshot.core.domain.pair.BatchCombineUseCase
import com.pairshot.core.domain.pair.GetPairsByProjectUseCase
import com.pairshot.core.model.PairStatus
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.domain.pair.PhotoPairRepository
import com.pairshot.core.domain.project.DeleteProjectUseCase
import com.pairshot.core.domain.project.ProjectRepository
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.domain.settings.WatermarkRepository
import com.pairshot.core.infra.image.WatermarkRenderer
import com.pairshot.core.ui.component.SnackbarEvent
import com.pairshot.core.ui.component.SnackbarVariant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DeleteConfirmation {
    data class PairsOnly(
        val count: Int,
    ) : DeleteConfirmation

    data class WithCombined(
        val pairCount: Int,
        val combinedCount: Int,
    ) : DeleteConfirmation
}

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
        private val combineSettingsRepository: CombineSettingsRepository,
        private val watermarkRepository: WatermarkRepository,
        val watermarkRenderer: WatermarkRenderer,
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

        private val _snackbarMessage = MutableSharedFlow<SnackbarEvent>()
        val snackbarMessage: SharedFlow<SnackbarEvent> = _snackbarMessage.asSharedFlow()

        private val _projectDeletedEvent = MutableSharedFlow<Unit>()
        val projectDeletedEvent: SharedFlow<Unit> = _projectDeletedEvent.asSharedFlow()

        private val _deleteConfirmation = MutableStateFlow<DeleteConfirmation?>(null)
        val deleteConfirmation: StateFlow<DeleteConfirmation?> = _deleteConfirmation.asStateFlow()

        val combinePreviewPair: StateFlow<PhotoPair?> =
            combine(_selectedIds, _uiState) { selectedIds, state ->
                if (state is GalleryUiState.Success && selectedIds.isNotEmpty()) {
                    val firstId = selectedIds.first()
                    state.pairs.find { it.id == firstId }
                } else {
                    null
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

        val combineConfig: StateFlow<CombineConfig> =
            combineSettingsRepository.configFlow
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = CombineConfig(),
                )

        val watermarkConfig: StateFlow<WatermarkConfig> =
            watermarkRepository.watermarkConfigFlow.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = WatermarkConfig(),
            )

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
            _selectedIds.value =
                _selectedIds.value.toMutableSet().apply {
                    if (!add(pairId)) remove(pairId)
                }
        }

        fun deselectAll() {
            _selectedIds.value = emptySet()
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

        fun combineSelected(
            applyWatermark: Boolean = false,
            combineConfigOverride: CombineConfig? = null,
        ) {
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
                    _snackbarMessage.emit(SnackbarEvent("합성할 수 있는 페어가 없습니다", SnackbarVariant.WARNING))
                    return@launch
                }
                val watermark = if (applyWatermark) watermarkConfig.value else null
                _combineProgress.value = CombineProgress(0, pairedIds.size)
                val result: BatchCombineResult =
                    batchCombineUseCase(pairedIds, watermark, combineConfigOverride) { current, total ->
                        _combineProgress.value = CombineProgress(current, total)
                    }
                _combineProgress.value = null
                val total = pairedIds.size
                val success = result.successCount
                val failed = result.failedIds.size
                val snackbarEvent =
                    if (failed == 0) {
                        SnackbarEvent("${success}개 합성완료", SnackbarVariant.SUCCESS)
                    } else {
                        SnackbarEvent("$success/${total}개 합성 완료. ${failed}개 실패", SnackbarVariant.WARNING)
                    }
                _snackbarMessage.emit(snackbarEvent)
                exitSelectionMode()
            }
        }

        fun onDeleteClick() {
            val state = _uiState.value as? GalleryUiState.Success ?: return
            val selectedPairs = state.pairs.filter { it.id in _selectedIds.value }
            val combinedCount = selectedPairs.count { it.status == PairStatus.COMBINED }
            _deleteConfirmation.value =
                if (combinedCount > 0) {
                    DeleteConfirmation.WithCombined(selectedPairs.size, combinedCount)
                } else {
                    DeleteConfirmation.PairsOnly(selectedPairs.size)
                }
        }

        fun dismissDeleteConfirmation() {
            _deleteConfirmation.value = null
        }

        fun confirmDeleteAll() {
            viewModelScope.launch {
                _deleteConfirmation.value = null
                val ids = _selectedIds.value.toList()
                val state = _uiState.value as? GalleryUiState.Success ?: return@launch
                ids.forEach { id ->
                    state.pairs.find { it.id == id }?.let { pair ->
                        photoPairRepository.delete(pair)
                    }
                }
                _snackbarMessage.emit(SnackbarEvent("${ids.size}개 삭제 완료", SnackbarVariant.INFO))
                exitSelectionMode()
            }
        }

        fun confirmDeleteCombinedOnly() {
            viewModelScope.launch {
                _deleteConfirmation.value = null
                val ids = _selectedIds.value.toList()
                val state = _uiState.value as? GalleryUiState.Success ?: return@launch
                val combinedIds =
                    ids.filter { id ->
                        state.pairs.find { it.id == id }?.status == PairStatus.COMBINED
                    }
                val total = combinedIds.size
                var successCount = 0
                combinedIds.forEach { id ->
                    try {
                        photoPairRepository.removeCombinedPhoto(id)
                        successCount++
                    } catch (_: Exception) {
                    }
                }
                val snackbarEvent =
                    when {
                        successCount == total -> SnackbarEvent("${successCount}개 합성 삭제", SnackbarVariant.SUCCESS)
                        successCount > 0 -> SnackbarEvent("$successCount/${total}개 삭제됨. 일부 실패", SnackbarVariant.WARNING)
                        else -> SnackbarEvent("삭제에 실패했습니다", SnackbarVariant.ERROR)
                    }
                _snackbarMessage.emit(snackbarEvent)
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
                    try {
                        deleteProjectUseCase(project)
                        _projectDeletedEvent.emit(Unit)
                    } catch (e: IllegalStateException) {
                        _snackbarMessage.emit(
                            SnackbarEvent(
                                "일부 사진을 삭제할 수 없어 프로젝트를 삭제하지 못했습니다",
                                SnackbarVariant.ERROR,
                            ),
                        )
                    }
                }
            }
        }
    }
