package com.pairshot.feature.project.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.feature.project.ui.screen.ProjectListScreen
import com.pairshot.feature.project.ui.viewmodel.ProjectViewModel

@Composable
fun ProjectListRoute(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProject: (Long) -> Unit = {},
    viewModel: ProjectViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectionMode by viewModel.selectionMode.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val groupMode by viewModel.groupMode.collectAsStateWithLifecycle()

    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteSelectedDialog by rememberSaveable { mutableStateOf(false) }
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showTopMenu by rememberSaveable { mutableStateOf(false) }

    ProjectListScreen(
        uiState = uiState,
        selectionMode = selectionMode,
        selectedIds = selectedIds,
        groupMode = groupMode,
        showCreateDialog = showCreateDialog,
        showDeleteSelectedDialog = showDeleteSelectedDialog,
        showRenameDialog = showRenameDialog,
        showTopMenu = showTopMenu,
        viewModel = viewModel,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToProject = onNavigateToProject,
        onShowCreateDialog = { showCreateDialog = true },
        onDismissCreateDialog = { showCreateDialog = false },
        onShowDeleteSelectedDialog = { showDeleteSelectedDialog = true },
        onDismissDeleteSelectedDialog = { showDeleteSelectedDialog = false },
        onShowRenameDialog = { showRenameDialog = true },
        onDismissRenameDialog = { showRenameDialog = false },
        onShowTopMenu = { showTopMenu = true },
        onDismissTopMenu = { showTopMenu = false },
    )
}
