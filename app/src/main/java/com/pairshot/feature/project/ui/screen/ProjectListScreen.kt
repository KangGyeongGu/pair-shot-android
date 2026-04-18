package com.pairshot.feature.project.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.feature.project.ui.component.ProjectItem
import com.pairshot.feature.project.ui.component.ProjectListTopBar
import com.pairshot.feature.project.ui.dialog.CreateProjectDialog
import com.pairshot.feature.project.ui.dialog.DeleteSelectedProjectsDialog
import com.pairshot.feature.project.ui.viewmodel.ProjectUiState
import com.pairshot.feature.project.ui.viewmodel.ProjectViewModel
import com.pairshot.ui.theme.PairShotSpacing

@Composable
internal fun ProjectListScreen(
    uiState: ProjectUiState,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    showCreateDialog: Boolean,
    showDeleteSelectedDialog: Boolean,
    showTopMenu: Boolean,
    viewModel: ProjectViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onShowCreateDialog: () -> Unit,
    onDismissCreateDialog: () -> Unit,
    onShowDeleteSelectedDialog: () -> Unit,
    onDismissDeleteSelectedDialog: () -> Unit,
    onShowTopMenu: () -> Unit,
    onDismissTopMenu: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ProjectListTopBar(
                selectionMode = selectionMode,
                selectedCount = selectedIds.size,
                showTopMenu = showTopMenu,
                onExitSelectionMode = viewModel::exitSelectionMode,
                onSelectAll = viewModel::selectAll,
                onShowTopMenu = onShowTopMenu,
                onDismissTopMenu = onDismissTopMenu,
                onEnterSelectionMode = viewModel::enterSelectionMode,
                onNavigateToSettings = onNavigateToSettings,
            )
        },
        bottomBar = {
            if (selectionMode) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = PairShotSpacing.screenPadding,
                                    vertical = PairShotSpacing.cardPadding,
                                ),
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
                                    style = MaterialTheme.typography.labelSmall,
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
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        contentPadding = PaddingValues(bottom = 96.dp),
                    ) {
                        items(
                            items = state.projects,
                            key = { it.id },
                        ) { project ->
                            ProjectItem(
                                project = project,
                                selectionMode = selectionMode,
                                isSelected = project.id in selectedIds,
                                onClick = {
                                    if (selectionMode) {
                                        viewModel.toggleSelection(project.id)
                                    } else {
                                        onNavigateToProject(project.id)
                                    }
                                },
                                onToggleSelection = { viewModel.toggleSelection(project.id) },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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
}
