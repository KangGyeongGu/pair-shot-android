package com.pairshot.feature.pair.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.domain.combine.CombineConfig
import com.pairshot.core.domain.pair.PairStatus
import com.pairshot.core.domain.pair.PhotoPair
import com.pairshot.core.domain.settings.WatermarkConfig
import com.pairshot.core.infra.image.WatermarkRenderer
import com.pairshot.core.ui.component.PairShotSnackbar
import com.pairshot.core.ui.component.PairShotSnackbarController
import com.pairshot.core.ui.component.TopProgressPill
import com.pairshot.feature.pair.ui.component.GalleryFilterRow
import com.pairshot.feature.pair.ui.component.GallerySelectionBottomBar
import com.pairshot.feature.pair.ui.component.GalleryTopBar
import com.pairshot.feature.pair.ui.component.PairGridSection
import com.pairshot.feature.pair.ui.dialog.CombinePreviewBottomSheet
import com.pairshot.feature.pair.ui.dialog.DeletePairsDialog
import com.pairshot.feature.pair.ui.dialog.DeleteProjectDialog
import com.pairshot.feature.pair.ui.dialog.DeleteWithCombinedDialog
import com.pairshot.feature.pair.ui.dialog.RenameProjectDialog
import com.pairshot.feature.pair.ui.viewmodel.CombineProgress
import com.pairshot.feature.pair.ui.viewmodel.DeleteConfirmation
import com.pairshot.feature.pair.ui.viewmodel.GalleryUiState

@Composable
internal fun GalleryScreen(
    uiState: GalleryUiState,
    showCombinedOnly: Boolean,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    combineProgress: CombineProgress?,
    combinePreviewPair: PhotoPair?,
    combineConfig: CombineConfig,
    watermarkConfig: WatermarkConfig,
    watermarkRenderer: WatermarkRenderer,
    snackbarController: PairShotSnackbarController,
    deleteConfirmation: DeleteConfirmation?,
    showMoreMenu: Boolean,
    showRenameDialog: Boolean,
    showProjectDeleteDialog: Boolean,
    showCombineDialog: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToAfterCamera: (Long) -> Unit,
    onNavigateToCompare: (Long) -> Unit,
    onNavigateToCombined: (Long) -> Unit,
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
    onDeleteClick: () -> Unit,
    onDismissDeleteConfirmation: () -> Unit,
    onConfirmDeleteAll: () -> Unit,
    onConfirmDeleteCombinedOnly: () -> Unit,
    onToggleFilter: () -> Unit,
    onToggleSelection: (Long) -> Unit,
    onLongPressSelect: (Long) -> Unit,
    onShowCombineDialog: () -> Unit,
    onDismissCombineDialog: () -> Unit,
    onCombineSelected: (applyWatermark: Boolean, combineConfigOverride: CombineConfig?) -> Unit,
    onNavigateToCombineSettings: () -> Unit,
    onNavigateToWatermarkSettings: () -> Unit,
    onRenameProject: (String) -> Unit,
    onDeleteProject: () -> Unit,
) {
    val projectName =
        when (val state = uiState) {
            is GalleryUiState.Success -> state.projectName
            else -> "갤러리"
        }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
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
                        onCombineSelected = onShowCombineDialog,
                        onExportSelected = { onNavigateToExport(selectedIds) },
                        onShowDeleteDialog = onDeleteClick,
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
                            onNavigateToCombined = onNavigateToCombined,
                        )
                    }
                }
            }
        }

        combineProgress?.let { progress ->
            TopProgressPill(
                label = "원본 합성 중 · ${progress.total}개",
                progress = progress.current.toFloat() / progress.total,
                progressText = "${progress.current}/${progress.total}",
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 8.dp),
            )
        }

        SnackbarHost(
            hostState = snackbarController.hostState,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp),
            snackbar = { data ->
                PairShotSnackbar(
                    message = data.visuals.message,
                    variant = snackbarController.currentVariant,
                    actionLabel = data.visuals.actionLabel,
                    onAction = { data.performAction() },
                )
            },
        )
    }

    when (val confirmation = deleteConfirmation) {
        is DeleteConfirmation.PairsOnly -> {
            DeletePairsDialog(
                selectedCount = confirmation.count,
                onDismiss = onDismissDeleteConfirmation,
                onConfirm = {
                    onDismissDeleteConfirmation()
                    onConfirmDeleteAll()
                },
            )
        }

        is DeleteConfirmation.WithCombined -> {
            DeleteWithCombinedDialog(
                pairCount = confirmation.pairCount,
                combinedCount = confirmation.combinedCount,
                onDismiss = onDismissDeleteConfirmation,
                onDeleteAll = {
                    onDismissDeleteConfirmation()
                    onConfirmDeleteAll()
                },
                onDeleteCombinedOnly = {
                    onDismissDeleteConfirmation()
                    onConfirmDeleteCombinedOnly()
                },
            )
        }

        null -> {
            Unit
        }
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

    if (showCombineDialog) {
        CombinePreviewBottomSheet(
            selectedCount = selectedIds.size,
            beforeUri = combinePreviewPair?.beforePhotoUri,
            afterUri = combinePreviewPair?.afterPhotoUri,
            combineConfig = combineConfig,
            watermarkConfig = watermarkConfig,
            watermarkRenderer = watermarkRenderer,
            onDismiss = onDismissCombineDialog,
            onCombineStart = { applyWatermark, configOverride -> onCombineSelected(applyWatermark, configOverride) },
            onNavigateToCombineSettings = onNavigateToCombineSettings,
            onNavigateToWatermarkSettings = onNavigateToWatermarkSettings,
        )
    }
}
