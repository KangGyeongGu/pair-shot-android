package com.pairshot.feature.pair.ui.route

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.feature.pair.ui.screen.GalleryScreen
import com.pairshot.feature.pair.ui.viewmodel.GalleryViewModel

@Composable
fun GalleryRoute(
    projectId: Long,
    onNavigateBack: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToAfterCamera: (Long) -> Unit = {},
    onNavigateToCompare: (Long) -> Unit = {},
    onNavigateToExport: (Set<Long>) -> Unit = {},
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showCombinedOnly by viewModel.showCombinedOnly.collectAsStateWithLifecycle()
    val selectionMode by viewModel.selectionMode.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val combineProgress by viewModel.combineProgress.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showProjectDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.projectDeletedEvent.collect {
            onNavigateBack()
        }
    }

    GalleryScreen(
        uiState = uiState,
        showCombinedOnly = showCombinedOnly,
        selectionMode = selectionMode,
        selectedIds = selectedIds,
        combineProgress = combineProgress,
        snackbarHostState = snackbarHostState,
        showDeleteDialog = showDeleteDialog,
        showMoreMenu = showMoreMenu,
        showRenameDialog = showRenameDialog,
        showProjectDeleteDialog = showProjectDeleteDialog,
        onNavigateBack = onNavigateBack,
        onNavigateToCamera = onNavigateToCamera,
        onNavigateToAfterCamera = onNavigateToAfterCamera,
        onNavigateToCompare = onNavigateToCompare,
        onNavigateToExport = onNavigateToExport,
        onExitSelectionMode = { viewModel.exitSelectionMode() },
        onSelectAll = { viewModel.selectAll() },
        onDeselectAll = { viewModel.deselectAll() },
        onShowMoreMenu = { showMoreMenu = true },
        onDismissMoreMenu = { showMoreMenu = false },
        onEnterSelectionMode = { viewModel.enterSelectionMode() },
        onShowRenameDialog = { showRenameDialog = true },
        onDismissRenameDialog = { showRenameDialog = false },
        onShowProjectDeleteDialog = { showProjectDeleteDialog = true },
        onDismissProjectDeleteDialog = { showProjectDeleteDialog = false },
        onShowDeleteDialog = { showDeleteDialog = true },
        onDismissDeleteDialog = { showDeleteDialog = false },
        onToggleFilter = { viewModel.toggleFilter() },
        onToggleSelection = { viewModel.toggleSelection(it) },
        onLongPressSelect = { viewModel.longPressSelect(it) },
        onCombineSelected = { viewModel.combineSelected() },
        onDeleteSelected = { viewModel.deleteSelected() },
        onRenameProject = { viewModel.renameProject(it) },
        onDeleteProject = { viewModel.deleteProject() },
    )
}
