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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.feature.album.R
import com.pairshot.feature.album.screen.AlbumDetailScreen
import com.pairshot.feature.album.viewmodel.AlbumDetailEvent
import com.pairshot.feature.album.viewmodel.AlbumDetailUiState
import com.pairshot.feature.album.viewmodel.AlbumDetailViewModel

@Composable
fun AlbumDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPairPreview: (pairId: Long) -> Unit,
    onNavigateToAfterCamera: (pairId: Long, albumId: Long) -> Unit,
    onNavigateToCamera: (albumId: Long) -> Unit,
    onNavigateToPairPicker: (albumId: Long) -> Unit,
    onNavigateToExportSettings: (pairIds: Set<Long>) -> Unit,
    onShareSelected: (pairIds: Set<Long>) -> Unit,
    onSaveSelectedToDevice: (pairIds: Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is AlbumDetailEvent.NavigateBack -> onNavigateBack()
                is AlbumDetailEvent.NavigateToPairPreview -> onNavigateToPairPreview(event.pairId)
                is AlbumDetailEvent.NavigateToAfterCamera -> onNavigateToAfterCamera(event.pairId, event.albumId)
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

        AlbumDetailUiState.Error -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.album_route_load_failed),
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
                onRemoveFromAlbumClick = viewModel::showDeletePairsDialog,
                onExportSettingsClick = { onNavigateToExportSettings(state.selectedIds) },
                onRenameClick = viewModel::showRenameDialog,
                onDeleteAlbumClick = viewModel::showDeleteAlbumDialog,
                onRenameConfirm = viewModel::confirmRenameAlbum,
                onRenameDismiss = viewModel::dismissRenameDialog,
                onDeleteAlbumConfirm = viewModel::confirmDeleteAlbum,
                onDeleteAlbumDismiss = viewModel::dismissDeleteAlbumDialog,
                onDeletePairsConfirm = viewModel::confirmDeletePairs,
                onDeletePairsDismiss = viewModel::dismissDeletePairsDialog,
                modifier = modifier,
            )
        }
    }
}
