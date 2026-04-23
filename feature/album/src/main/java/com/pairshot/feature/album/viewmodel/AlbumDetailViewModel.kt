package com.pairshot.feature.album.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.core.domain.album.AlbumRepository
import com.pairshot.core.domain.album.DeleteAlbumUseCase
import com.pairshot.core.domain.album.RenameAlbumUseCase
import com.pairshot.core.domain.combine.DeleteCombinedPhotosUseCase
import com.pairshot.core.domain.pair.DeletePairsUseCase
import com.pairshot.core.domain.pair.PairNavigationTarget
import com.pairshot.core.domain.pair.ResolvePairNavigationTargetUseCase
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.model.Album
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.model.SortOrder
import com.pairshot.core.navigation.AlbumDetail
import com.pairshot.core.ui.text.UiText
import com.pairshot.feature.album.R
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

private const val WHILE_SUBSCRIBED_TIMEOUT_MS = 5_000L

sealed interface AlbumDetailEvent {
    data object NavigateBack : AlbumDetailEvent

    data class NavigateToPairPreview(
        val pairId: Long,
    ) : AlbumDetailEvent

    data class NavigateToAfterCamera(
        val pairId: Long,
        val albumId: Long,
    ) : AlbumDetailEvent

    data class NavigateToCamera(
        val albumId: Long,
    ) : AlbumDetailEvent

    data class NavigateToPairPicker(
        val albumId: Long,
    ) : AlbumDetailEvent
}

sealed interface AlbumDetailUiState {
    data object Loading : AlbumDetailUiState

    data class Error(
        val message: UiText,
    ) : AlbumDetailUiState

    data class Success(
        val album: Album,
        val pairs: List<PhotoPair>,
        val selectedIds: Set<Long> = emptySet(),
        val isSelectionMode: Boolean = false,
        val showRenameDialog: Boolean = false,
        val showDeleteAlbumDialog: Boolean = false,
        val showDeletePairsDialog: Boolean = false,
        val sortOrder: SortOrder = SortOrder.DESC,
    ) : AlbumDetailUiState
}

private sealed interface AlbumContentPhase {
    data object Loading : AlbumContentPhase

    data object Error : AlbumContentPhase

    data class Loaded(
        val album: Album,
        val pairs: List<PhotoPair>,
    ) : AlbumContentPhase
}

private data class DialogState(
    val showRename: Boolean = false,
    val showDeleteAlbum: Boolean = false,
    val showDeletePairs: Boolean = false,
)

private data class SelectionState(
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
)

@HiltViewModel
class AlbumDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val albumRepository: AlbumRepository,
        private val deleteAlbumUseCase: DeleteAlbumUseCase,
        private val renameAlbumUseCase: RenameAlbumUseCase,
        private val resolvePairNavigationTargetUseCase: ResolvePairNavigationTargetUseCase,
        private val deletePairsUseCase: DeletePairsUseCase,
        private val deleteCombinedPhotosUseCase: DeleteCombinedPhotosUseCase,
        private val appSettingsRepository: AppSettingsRepository,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<AlbumDetail>()
        val albumId: Long = route.albumId

        private val _contentPhase = MutableStateFlow<AlbumContentPhase>(AlbumContentPhase.Loading)
        private val _selection = MutableStateFlow(SelectionState())
        private val _dialogs = MutableStateFlow(DialogState())

        private var latestAlbum: Album? = null
        private var latestPairs: List<PhotoPair> = emptyList()

        val sortOrder: StateFlow<SortOrder> =
            appSettingsRepository.albumSortOrderFlow.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = SortOrder.DESC,
            )

        val uiState: StateFlow<AlbumDetailUiState> =
            combine(
                _contentPhase,
                _selection,
                _dialogs,
                sortOrder,
            ) { phase, selection, dialogs, sort ->
                when (phase) {
                    AlbumContentPhase.Loading -> {
                        AlbumDetailUiState.Loading
                    }

                    AlbumContentPhase.Error -> {
                        AlbumDetailUiState.Error(UiText.Resource(R.string.album_route_load_failed))
                    }

                    is AlbumContentPhase.Loaded -> {
                        AlbumDetailUiState.Success(
                            album = phase.album,
                            pairs = phase.pairs,
                            selectedIds = selection.selectedIds,
                            isSelectionMode = selection.isSelectionMode,
                            showRenameDialog = dialogs.showRename,
                            showDeleteAlbumDialog = dialogs.showDeleteAlbum,
                            showDeletePairsDialog = dialogs.showDeletePairs,
                            sortOrder = sort,
                        )
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT_MS), AlbumDetailUiState.Loading)

        private val _events = MutableSharedFlow<AlbumDetailEvent>()
        val events: SharedFlow<AlbumDetailEvent> = _events.asSharedFlow()

        fun toggleSortOrder() {
            viewModelScope.launch {
                val next = if (sortOrder.value == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
                appSettingsRepository.updateAlbumSortOrder(next)
            }
        }

        init {
            loadAlbum()
            observePairs()
        }

        private fun loadAlbum() {
            viewModelScope.launch {
                val album = albumRepository.getById(albumId)
                if (album == null) {
                    _contentPhase.value = AlbumContentPhase.Error
                    return@launch
                }
                latestAlbum = album
                emitLoadedIfReady()
            }
        }

        private fun observePairs() {
            viewModelScope.launch {
                albumRepository.observePairs(albumId).collect { pairs ->
                    latestPairs = pairs
                    emitLoadedIfReady()
                }
            }
        }

        private fun emitLoadedIfReady() {
            val album = latestAlbum ?: return
            _contentPhase.value = AlbumContentPhase.Loaded(album, latestPairs)
        }

        fun onPairClick(pairId: Long) {
            if (_selection.value.isSelectionMode) {
                toggleSelection(pairId)
                return
            }
            val pair = latestPairs.firstOrNull { it.id == pairId } ?: return
            viewModelScope.launch {
                val event =
                    when (val target = resolvePairNavigationTargetUseCase(pair)) {
                        is PairNavigationTarget.AfterCamera -> AlbumDetailEvent.NavigateToAfterCamera(target.pairId, albumId)
                        is PairNavigationTarget.PairPreview -> AlbumDetailEvent.NavigateToPairPreview(target.pairId)
                    }
                _events.emit(event)
            }
        }

        fun onPairLongPress(pairId: Long) {
            _selection.update { it.copy(isSelectionMode = true) }
            toggleSelection(pairId)
        }

        private fun toggleSelection(pairId: Long) {
            _selection.update { state ->
                val updated =
                    if (pairId in state.selectedIds) {
                        state.selectedIds - pairId
                    } else {
                        state.selectedIds + pairId
                    }
                state.copy(
                    selectedIds = updated,
                    isSelectionMode = updated.isNotEmpty(),
                )
            }
        }

        fun exitSelectionMode() {
            _selection.value = SelectionState()
        }

        fun onFabClick() {
            viewModelScope.launch {
                _events.emit(AlbumDetailEvent.NavigateToCamera(albumId))
            }
        }

        fun onAddPairsClick() {
            viewModelScope.launch {
                _events.emit(AlbumDetailEvent.NavigateToPairPicker(albumId))
            }
        }

        fun showDeletePairsDialog() {
            _dialogs.update { it.copy(showDeletePairs = true) }
        }

        fun dismissDeletePairsDialog() {
            _dialogs.update { it.copy(showDeletePairs = false) }
        }

        fun removeSelectedFromAlbum() {
            val selectedIds = _selection.value.selectedIds.toList()
            viewModelScope.launch {
                if (selectedIds.isNotEmpty()) albumRepository.removePairs(albumId, selectedIds)
                closeSelectionAndDialog()
            }
        }

        fun deleteSelectedPairs() {
            val selectedIds = _selection.value.selectedIds
            if (selectedIds.isEmpty()) {
                closeSelectionAndDialog()
                return
            }
            val pairsToDelete = latestPairs.filter { it.id in selectedIds }
            viewModelScope.launch {
                deletePairsUseCase(pairsToDelete)
                closeSelectionAndDialog()
            }
        }

        fun deleteSelectedCombinedOnly() {
            val selectedIds = _selection.value.selectedIds.toList()
            viewModelScope.launch {
                if (selectedIds.isNotEmpty()) deleteCombinedPhotosUseCase(selectedIds)
                closeSelectionAndDialog()
            }
        }

        private fun closeSelectionAndDialog() {
            _dialogs.update { it.copy(showDeletePairs = false) }
            _selection.value = SelectionState()
        }

        fun showRenameDialog() {
            _dialogs.update { it.copy(showRename = true) }
        }

        fun dismissRenameDialog() {
            _dialogs.update { it.copy(showRename = false) }
        }

        fun confirmRenameAlbum(newName: String) {
            viewModelScope.launch {
                renameAlbumUseCase(albumId, newName)
                latestAlbum = latestAlbum?.copy(name = newName.trim())
                emitLoadedIfReady()
                _dialogs.update { it.copy(showRename = false) }
            }
        }

        fun showDeleteAlbumDialog() {
            _dialogs.update { it.copy(showDeleteAlbum = true) }
        }

        fun dismissDeleteAlbumDialog() {
            _dialogs.update { it.copy(showDeleteAlbum = false) }
        }

        fun confirmDeleteAlbum() {
            viewModelScope.launch {
                deleteAlbumUseCase(albumId)
                _dialogs.update { it.copy(showDeleteAlbum = false) }
                _events.emit(AlbumDetailEvent.NavigateBack)
            }
        }
    }
