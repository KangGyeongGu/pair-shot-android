package com.pairshot.feature.album.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.core.domain.album.AlbumRepository
import com.pairshot.core.domain.pair.PhotoPairRepository
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.navigation.PairPicker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PairPickerEvent {
    data object NavigateBack : PairPickerEvent
}

data class PairPickerUiState(
    val pairs: List<PhotoPair> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val alreadyInAlbumIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val isConfirming: Boolean = false,
)

@HiltViewModel
class PairPickerViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val photoPairRepository: PhotoPairRepository,
        private val albumRepository: AlbumRepository,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<PairPicker>()
        val albumId: Long = route.albumId

        private val _uiState = MutableStateFlow(PairPickerUiState())
        val uiState: StateFlow<PairPickerUiState> = _uiState.asStateFlow()

        private val _events = MutableSharedFlow<PairPickerEvent>()
        val events: SharedFlow<PairPickerEvent> = _events.asSharedFlow()

        init {
            observeAllPairs()
            observeAlbumPairs()
        }

        private fun observeAllPairs() {
            viewModelScope.launch {
                photoPairRepository.observeAll().collect { pairs ->
                    _uiState.update { it.copy(pairs = pairs, isLoading = false) }
                }
            }
        }

        private fun observeAlbumPairs() {
            viewModelScope.launch {
                albumRepository.observePairs(albumId).collect { albumPairs ->
                    _uiState.update { it.copy(alreadyInAlbumIds = albumPairs.map { p -> p.id }.toSet()) }
                }
            }
        }

        fun toggleSelection(pairId: Long) {
            _uiState.update { state ->
                val updated =
                    if (pairId in state.selectedIds) {
                        state.selectedIds - pairId
                    } else {
                        state.selectedIds + pairId
                    }
                state.copy(selectedIds = updated)
            }
        }

        fun confirmSelection() {
            val selectedIds = _uiState.value.selectedIds.toList()
            if (selectedIds.isEmpty()) {
                viewModelScope.launch { _events.emit(PairPickerEvent.NavigateBack) }
                return
            }
            _uiState.update { it.copy(isConfirming = true) }
            viewModelScope.launch {
                albumRepository.addPairs(albumId, selectedIds)
                _events.emit(PairPickerEvent.NavigateBack)
            }
        }
    }
