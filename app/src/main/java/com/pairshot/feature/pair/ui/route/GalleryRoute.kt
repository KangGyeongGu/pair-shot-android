package com.pairshot.feature.pair.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.core.domain.combine.CombineConfig
import com.pairshot.core.domain.pair.PhotoPair
import com.pairshot.core.ui.component.PairShotSnackbarController
import com.pairshot.feature.pair.ui.screen.GalleryScreen
import com.pairshot.feature.pair.ui.viewmodel.GalleryViewModel

@Composable
fun GalleryRoute(
    projectId: Long,
    onNavigateBack: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToAfterCamera: (Long) -> Unit = {},
    onNavigateToCompare: (Long) -> Unit = {},
    onNavigateToCombined: (Long) -> Unit = {},
    onNavigateToExport: (Set<Long>) -> Unit = {},
    onNavigateToCombineSettings: () -> Unit = {},
    onNavigateToWatermarkSettings: () -> Unit = {},
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showCombinedOnly by viewModel.showCombinedOnly.collectAsStateWithLifecycle()
    val selectionMode by viewModel.selectionMode.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val combineProgress by viewModel.combineProgress.collectAsStateWithLifecycle()
    val combinePreviewPair by viewModel.combinePreviewPair.collectAsStateWithLifecycle()
    val combineConfig by viewModel.combineConfig.collectAsStateWithLifecycle()
    val watermarkConfig by viewModel.watermarkConfig.collectAsStateWithLifecycle()

    val deleteConfirmation by viewModel.deleteConfirmation.collectAsStateWithLifecycle()
    val snackbarController = remember { PairShotSnackbarController() }
    val hapticFeedback = LocalHapticFeedback.current
    var showMoreMenu by rememberSaveable { mutableStateOf(false) }
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showProjectDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showCombineDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { event ->
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            snackbarController.show(event)
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
        combinePreviewPair = combinePreviewPair,
        combineConfig = combineConfig,
        watermarkConfig = watermarkConfig,
        watermarkRenderer = viewModel.watermarkRenderer,
        snackbarController = snackbarController,
        deleteConfirmation = deleteConfirmation,
        showMoreMenu = showMoreMenu,
        showRenameDialog = showRenameDialog,
        showProjectDeleteDialog = showProjectDeleteDialog,
        showCombineDialog = showCombineDialog,
        onNavigateBack = onNavigateBack,
        onNavigateToCamera = onNavigateToCamera,
        onNavigateToAfterCamera = onNavigateToAfterCamera,
        onNavigateToCompare = onNavigateToCompare,
        onNavigateToCombined = onNavigateToCombined,
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
        onDeleteClick = viewModel::onDeleteClick,
        onDismissDeleteConfirmation = viewModel::dismissDeleteConfirmation,
        onConfirmDeleteAll = viewModel::confirmDeleteAll,
        onConfirmDeleteCombinedOnly = viewModel::confirmDeleteCombinedOnly,
        onToggleFilter = { viewModel.toggleFilter() },
        onToggleSelection = { viewModel.toggleSelection(it) },
        onLongPressSelect = { viewModel.longPressSelect(it) },
        onShowCombineDialog = { showCombineDialog = true },
        onDismissCombineDialog = { showCombineDialog = false },
        onCombineSelected = { applyWatermark, combineConfigOverride ->
            viewModel.combineSelected(applyWatermark, combineConfigOverride)
        },
        onNavigateToCombineSettings = onNavigateToCombineSettings,
        onNavigateToWatermarkSettings = onNavigateToWatermarkSettings,
        onRenameProject = { viewModel.renameProject(it) },
        onDeleteProject = { viewModel.deleteProject() },
    )
}
