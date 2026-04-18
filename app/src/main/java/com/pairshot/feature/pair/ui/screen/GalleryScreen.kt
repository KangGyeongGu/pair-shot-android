package com.pairshot.feature.pair.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.domain.pair.PairStatus
import com.pairshot.core.domain.pair.PhotoPair
import com.pairshot.feature.pair.ui.component.GalleryFilterRow
import com.pairshot.feature.pair.ui.component.GallerySelectionBottomBar
import com.pairshot.feature.pair.ui.component.GalleryTopBar
import com.pairshot.feature.pair.ui.component.PairGridSection
import com.pairshot.feature.pair.ui.dialog.CombineProgressDialog
import com.pairshot.feature.pair.ui.dialog.DeletePairsDialog
import com.pairshot.feature.pair.ui.dialog.DeleteProjectDialog
import com.pairshot.feature.pair.ui.dialog.RenameProjectDialog
import com.pairshot.feature.pair.ui.viewmodel.CombineProgress
import com.pairshot.feature.pair.ui.viewmodel.GalleryUiState

@Composable
internal fun GalleryScreen(
    uiState: GalleryUiState,
    showCombinedOnly: Boolean,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    combineProgress: CombineProgress?,
    snackbarHostState: SnackbarHostState,
    showDeleteDialog: Boolean,
    showMoreMenu: Boolean,
    showRenameDialog: Boolean,
    showProjectDeleteDialog: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToAfterCamera: (Long) -> Unit,
    onNavigateToCompare: (Long) -> Unit,
    onNavigateToExport: (Set<Long>) -> Unit,
    onExitSelectionMode: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onShowMoreMenu: () -> Unit,
    onDismissMoreMenu: () -> Unit,
    onEnterSelectionMode: () -> Unit,
    onShowRenameDialog: () -> Unit,
    onDismissRenameDialog: () -> Unit,
    onShowProjectDeleteDialog: () -> Unit,
    onDismissProjectDeleteDialog: () -> Unit,
    onShowDeleteDialog: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onToggleFilter: () -> Unit,
    onToggleSelection: (Long) -> Unit,
    onLongPressSelect: (Long) -> Unit,
    onCombineSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onRenameProject: (String) -> Unit,
    onDeleteProject: () -> Unit,
) {
    val projectName =
        when (val state = uiState) {
            is GalleryUiState.Success -> state.projectName
            else -> "갤러리"
        }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GalleryTopBar(
                projectName = projectName,
                selectionMode = selectionMode,
                selectedCount = selectedIds.size,
                totalCount =
                    when (val state = uiState) {
                        is GalleryUiState.Success -> state.pairs.size
                        else -> 0
                    },
                showMoreMenu = showMoreMenu,
                onExitSelectionMode = onExitSelectionMode,
                onNavigateBack = onNavigateBack,
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll,
                onShowMoreMenu = onShowMoreMenu,
                onDismissMoreMenu = onDismissMoreMenu,
                onEnterSelectionMode = onEnterSelectionMode,
                onShowRenameDialog = onShowRenameDialog,
                onShowProjectDeleteDialog = onShowProjectDeleteDialog,
            )
        },
        bottomBar = {
            if (selectionMode) {
                GallerySelectionBottomBar(
                    selectedCount = selectedIds.size,
                    onCombineSelected = onCombineSelected,
                    onExportSelected = { onNavigateToExport(selectedIds) },
                    onShowDeleteDialog = onShowDeleteDialog,
                )
            }
        },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(
                    onClick = onNavigateToCamera,
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Before 촬영",
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        when (val state = uiState) {
            is GalleryUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is GalleryUiState.Error -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            is GalleryUiState.Success -> {
                val displayedPairs =
                    remember(state.pairs, showCombinedOnly) {
                        if (showCombinedOnly) {
                            state.pairs.filter { it.status == PairStatus.COMBINED }
                        } else {
                            state.pairs
                        }
                    }

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                ) {
                    Spacer(modifier = Modifier.height(PairShotSpacing.cardPadding))
                    GalleryFilterRow(
                        totalCount = state.pairs.size,
                        combinedCount = state.combinedCount,
                        showCombinedOnly = showCombinedOnly,
                        onToggleFilter = onToggleFilter,
                    )
                    Spacer(modifier = Modifier.height(PairShotSpacing.cardPadding))
                    PairGridSection(
                        pairs = displayedPairs,
                        showCombinedOnly = showCombinedOnly,
                        selectionMode = selectionMode,
                        selectedIds = selectedIds,
                        onToggleSelection = onToggleSelection,
                        onLongPressSelect = onLongPressSelect,
                        onNavigateToAfterCamera = onNavigateToAfterCamera,
                        onNavigateToCompare = onNavigateToCompare,
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeletePairsDialog(
            selectedCount = selectedIds.size,
            onDismiss = onDismissDeleteDialog,
            onConfirm = {
                onDismissDeleteDialog()
                onDeleteSelected()
            },
        )
    }

    if (showRenameDialog) {
        RenameProjectDialog(
            currentName = projectName,
            onDismiss = onDismissRenameDialog,
            onConfirm = { newName ->
                onRenameProject(newName)
                onDismissRenameDialog()
            },
        )
    }

    if (showProjectDeleteDialog) {
        DeleteProjectDialog(
            projectName = projectName,
            onDismiss = onDismissProjectDeleteDialog,
            onConfirm = {
                onDismissProjectDeleteDialog()
                onDeleteProject()
            },
        )
    }

    combineProgress?.let { progress ->
        CombineProgressDialog(progress = progress)
    }
}
