package com.pairshot.feature.project.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.designsystem.PairShotTypographyTokens
import com.pairshot.feature.project.ui.component.ProjectGroupCard
import com.pairshot.feature.project.ui.component.ProjectGroupFilterRow
import com.pairshot.feature.project.ui.component.ProjectGroupLabel
import com.pairshot.feature.project.ui.component.ProjectListTopBar
import com.pairshot.feature.project.ui.component.groupProjectsForDisplay
import com.pairshot.feature.project.ui.dialog.CreateProjectDialog
import com.pairshot.feature.project.ui.dialog.DeleteSelectedProjectsDialog
import com.pairshot.feature.project.ui.dialog.RenameProjectDialog
import com.pairshot.feature.project.ui.viewmodel.ProjectGroupMode
import com.pairshot.feature.project.ui.viewmodel.ProjectUiState
import com.pairshot.feature.project.ui.viewmodel.ProjectViewModel

@Composable
internal fun ProjectListScreen(
    uiState: ProjectUiState,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    groupMode: ProjectGroupMode,
    showCreateDialog: Boolean,
    showDeleteSelectedDialog: Boolean,
    showRenameDialog: Boolean,
    showTopMenu: Boolean,
    viewModel: ProjectViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onShowCreateDialog: () -> Unit,
    onDismissCreateDialog: () -> Unit,
    onShowDeleteSelectedDialog: () -> Unit,
    onDismissDeleteSelectedDialog: () -> Unit,
    onShowRenameDialog: () -> Unit,
    onDismissRenameDialog: () -> Unit,
    onShowTopMenu: () -> Unit,
    onDismissTopMenu: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ProjectListTopBar(
                selectionMode = selectionMode,
                selectedCount = selectedIds.size,
                totalCount = (uiState as? ProjectUiState.Success)?.projects?.size ?: 0,
                showTopMenu = showTopMenu,
                onExitSelectionMode = viewModel::exitSelectionMode,
                onSelectAll = viewModel::selectAll,
                onDeselectAll = viewModel::exitSelectionMode,
                onShowTopMenu = onShowTopMenu,
                onDismissTopMenu = onDismissTopMenu,
                onEnterSelectionMode = viewModel::enterSelectionMode,
                onNavigateToSettings = onNavigateToSettings,
            )
        },
        bottomBar = {
            if (selectionMode) {
                Surface(
                    modifier = Modifier.navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(PairShotSpacing.actionBar)
                                .padding(horizontal = PairShotSpacing.screenPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    PairShotSpacing.sectionGap,
                                    Alignment.CenterHorizontally,
                                ),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val editEnabled = selectedIds.size == 1
                                IconButton(
                                    onClick = onShowRenameDialog,
                                    enabled = editEnabled,
                                ) {
                                    Icon(
                                        imageVector = if (editEnabled) Icons.Default.Edit else Icons.Filled.EditOff,
                                        contentDescription = "이름 변경",
                                        tint =
                                            if (editEnabled) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.outline
                                            },
                                    )
                                }
                                Text(
                                    text = "수정",
                                    style = PairShotTypographyTokens.labelExtraSmall,
                                    color =
                                        if (editEnabled) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        },
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onShowDeleteSelectedDialog,
                                    enabled = selectedIds.isNotEmpty(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = "선택 삭제",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                                Text(
                                    text = "삭제",
                                    style = PairShotTypographyTokens.labelExtraSmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(
                    onClick = onShowCreateDialog,
                    modifier = Modifier.navigationBarsPadding(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "새 프로젝트 만들기",
                    )
                }
            }
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is ProjectUiState.Loading -> {
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

            is ProjectUiState.Error -> {
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

            is ProjectUiState.Success -> {
                if (state.projects.isEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "새 프로젝트를 만들어보세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    val groups =
                        remember(state.projects, groupMode) {
                            groupProjectsForDisplay(
                                projects = state.projects,
                                mode = groupMode,
                            )
                        }
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        contentPadding =
                            PaddingValues(
                                top = PairShotSpacing.cardPadding,
                                bottom = PairShotSpacing.fabOffset,
                            ),
                    ) {
                        item(key = "group_filter") {
                            ProjectGroupFilterRow(
                                groupMode = groupMode,
                                onGroupModeChange = viewModel::setGroupMode,
                            )
                            Spacer(modifier = Modifier.height(PairShotSpacing.cardPadding))
                        }

                        items(
                            items = groups,
                            key = { it.key },
                        ) { group ->
                            ProjectGroupLabel(label = group.label ?: "")
                            Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))

                            ProjectGroupCard(
                                projects = group.projects,
                                selectionMode = selectionMode,
                                selectedIds = selectedIds,
                                onProjectClick = { projectId ->
                                    if (selectionMode) {
                                        viewModel.toggleSelection(projectId)
                                    } else {
                                        onNavigateToProject(projectId)
                                    }
                                },
                                onProjectToggleSelection = viewModel::toggleSelection,
                            )
                            Spacer(modifier = Modifier.height(PairShotSpacing.cardPadding))
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            viewModel = viewModel,
            onDismiss = onDismissCreateDialog,
            onCreate = { name ->
                viewModel.createProject(name)
                onDismissCreateDialog()
            },
        )
    }

    if (showDeleteSelectedDialog) {
        DeleteSelectedProjectsDialog(
            selectedCount = selectedIds.size,
            onDismiss = onDismissDeleteSelectedDialog,
            onConfirm = {
                onDismissDeleteSelectedDialog()
                viewModel.deleteSelected()
            },
        )
    }

    if (showRenameDialog) {
        val selectedProject =
            (uiState as? ProjectUiState.Success)
                ?.projects
                ?.find { it.id in selectedIds }
        selectedProject?.let { project ->
            RenameProjectDialog(
                currentName = project.name,
                onDismiss = onDismissRenameDialog,
                onRename = { newName ->
                    viewModel.renameProject(project, newName)
                    onDismissRenameDialog()
                    viewModel.exitSelectionMode()
                },
            )
        }
    }
}
