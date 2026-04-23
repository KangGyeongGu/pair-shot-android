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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PairPickerEvent {
    data object NavigateBack : PairPickerEvent
}

sealed interface PairPickerUiState {
    data object Loading : PairPickerUiState

    data class Ready(
        val pairs: List<PhotoPair>,
        val alreadyInAlbumIds: Set<Long>,
        val selectedIds: Set<Long>,
        val isConfirming: Boolean,
    ) : PairPickerUiState
}

private data class PickerSelectionState(
    val selectedIds: Set<Long> = emptySet(),
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

        private val _pairs = MutableStateFlow<List<PhotoPair>?>(null)
        private val _albumPairIds = MutableStateFlow<Set<Long>>(emptySet())
        private val _selection = MutableStateFlow(PickerSelectionState())

        val uiState: StateFlow<PairPickerUiState> =
            combine(_pairs, _albumPairIds, _selection) { pairs, albumIds, selection ->
                if (pairs == null) {
                    PairPickerUiState.Loading
                } else {
                    PairPickerUiState.Ready(
                        pairs = pairs,
                        alreadyInAlbumIds = albumIds,
                        selectedIds = selection.selectedIds,
                        isConfirming = selection.isConfirming,
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PairPickerUiState.Loading,
            )

        private val _events = MutableSharedFlow<PairPickerEvent>()
        val events: SharedFlow<PairPickerEvent> = _events.asSharedFlow()

        init {
            observeAllPairs()
            observeAlbumPairs()
        }

        private fun observeAllPairs() {
            viewModelScope.launch {
                photoPairRepository.observeAll().collect { pairs ->
                    _pairs.value = pairs
                }
            }
        }

        private fun observeAlbumPairs() {
            viewModelScope.launch {
                albumRepository.observePairs(albumId).collect { albumPairs ->
                    _albumPairIds.value = albumPairs.map { it.id }.toSet()
                }
            }
        }

        fun toggleSelection(pairId: Long) {
            _selection.update { state ->
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
            val selectedIds = _selection.value.selectedIds.toList()
            if (selectedIds.isEmpty()) {
                viewModelScope.launch { _events.emit(PairPickerEvent.NavigateBack) }
                return
            }
            _selection.update { it.copy(isConfirming = true) }
            viewModelScope.launch {
                albumRepository.addPairs(albumId, selectedIds)
                _events.emit(PairPickerEvent.NavigateBack)
            }
        }
    }
