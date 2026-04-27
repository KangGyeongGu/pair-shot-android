package com.pairshot.feature.album.route

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.feature.album.screen.AlbumDetailScreen
import com.pairshot.feature.album.viewmodel.AlbumDetailEvent
import com.pairshot.feature.album.viewmodel.AlbumDetailUiState
import com.pairshot.feature.album.viewmodel.AlbumDetailViewModel

@Composable
fun AlbumDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPairPreview: (pairId: Long) -> Unit,
    onNavigateToAfterCamera: (pairId: Long, albumId: Long) -> Unit,
    onNavigateToBeforeRetake: (pairId: Long) -> Unit,
    onNavigateToCamera: (albumId: Long) -> Unit,
    onNavigateToPairPicker: (albumId: Long) -> Unit,
    onNavigateToExportSettings: (pairIds: Set<Long>) -> Unit,
    onShareSelected: (pairIds: Set<Long>) -> Unit,
    onSaveSelectedToDevice: (pairIds: Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is AlbumDetailEvent.NavigateBack -> onNavigateBack()
                is AlbumDetailEvent.NavigateToPairPreview -> onNavigateToPairPreview(event.pairId)
                is AlbumDetailEvent.NavigateToAfterCamera -> onNavigateToAfterCamera(event.pairId, event.albumId)
                is AlbumDetailEvent.NavigateToBeforeRetake -> onNavigateToBeforeRetake(event.pairId)
                is AlbumDetailEvent.NavigateToCamera -> onNavigateToCamera(event.albumId)
                is AlbumDetailEvent.NavigateToPairPicker -> onNavigateToPairPicker(event.albumId)
            }
        }
    }

    when (val state = uiState) {
        AlbumDetailUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is AlbumDetailUiState.Error -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = state.message.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        is AlbumDetailUiState.Success -> {
            AlbumDetailScreen(
                uiState = state,
                onNavigateBack = onNavigateBack,
                onPairClick = viewModel::onPairClick,
                onPairLongPress = viewModel::onPairLongPress,
                onExitSelectionMode = viewModel::exitSelectionMode,
                onCaptureBeforeClick = viewModel::onFabClick,
                onAddPairsClick = viewModel::onAddPairsClick,
                onShareClick = { onShareSelected(state.selectedIds) },
                onSaveToDeviceClick = { onSaveSelectedToDevice(state.selectedIds) },
                onDeleteClick = viewModel::showDeletePairsDialog,
                onExportSettingsClick = { onNavigateToExportSettings(state.selectedIds) },
                onRenameClick = viewModel::showRenameDialog,
                onDeleteAlbumClick = viewModel::showDeleteAlbumDialog,
                onRenameConfirm = viewModel::confirmRenameAlbum,
                onRenameDismiss = viewModel::dismissRenameDialog,
                onDeleteAlbumConfirm = viewModel::confirmDeleteAlbum,
                onDeleteAlbumDismiss = viewModel::dismissDeleteAlbumDialog,
                onRemoveFromAlbum = viewModel::removeSelectedFromAlbum,
                onDeletePairs = viewModel::deleteSelectedPairs,
                onDeleteCombinedOnly = viewModel::deleteSelectedCombinedOnly,
                onDeletePairsDismiss = viewModel::dismissDeletePairsDialog,
                onToggleSortOrder = viewModel::toggleSortOrder,
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = modifier,
            )
        }
    }
}
