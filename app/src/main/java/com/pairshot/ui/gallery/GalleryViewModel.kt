package com.pairshot.ui.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.domain.model.PairStatus
import com.pairshot.domain.model.PhotoPair
import com.pairshot.domain.repository.ProjectRepository
import com.pairshot.domain.usecase.pair.GetPairsByProjectUseCase
import com.pairshot.ui.navigation.ProjectDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

@HiltViewModel
class GalleryViewModel
    @Inject
    constructor(
        private val getPairsUseCase: GetPairsByProjectUseCase,
        private val projectRepository: ProjectRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val projectId: Long = savedStateHandle.toRoute<ProjectDetail>().projectId

        private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
        val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

        private val _showCombinedOnly = MutableStateFlow(false)
        val showCombinedOnly: StateFlow<Boolean> = _showCombinedOnly.asStateFlow()

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
    }
