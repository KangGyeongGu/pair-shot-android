package com.pairshot.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.domain.model.PairStatus
import com.pairshot.ui.component.CombinedCard
import com.pairshot.ui.component.PairCard
import com.pairshot.ui.theme.PairShotSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    projectId: Long,
    onNavigateBack: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToPairing: (Long) -> Unit = {},
    onNavigateToCompare: (Long) -> Unit = {},
    onNavigateToExport: (Set<Long>) -> Unit = {},
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showCombinedOnly by viewModel.showCombinedOnly.collectAsStateWithLifecycle()
    val selectionMode by viewModel.selectionMode.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val combineProgress by viewModel.combineProgress.collectAsStateWithLifecycle()

    val projectName =
        when (val state = uiState) {
            is GalleryUiState.Success -> state.projectName
            else -> "갤러리"
        }

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectionMode) "${selectedIds.size}개 선택됨" else projectName,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    if (selectionMode) {
                        IconButton(onClick = { viewModel.exitSelectionMode() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "선택 해제",
                            )
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기",
                            )
                        }
                    }
                },
                actions = {
                    if (selectionMode) {
                        TextButton(onClick = { viewModel.selectAll() }) {
                            Text(text = "전체선택")
                        }
                    } else {
                        TextButton(onClick = { viewModel.enterSelectionMode() }) {
                            Text(text = "선택")
                        }
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "더보기",
                                )
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("프로젝트명 수정") },
                                    onClick = {
                                        showMoreMenu = false // Step 2-4
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("프로젝트 삭제") },
                                    onClick = {
                                        showMoreMenu = false // Step 2-4
                                    },
                                )
                            }
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        bottomBar = {
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
                    if (selectionMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { viewModel.combineSelected() }) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = "합성",
                                    )
                                }
                                Text(
                                    text = "합성",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { onNavigateToExport(selectedIds) }) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "공유",
                                    )
                                }
                                Text(
                                    text = "공유",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "삭제",
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
                    } else {
                        Button(
                            onClick = onNavigateToCamera,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                            )
                            Text(
                                text = "Before 촬영",
                                modifier = Modifier.padding(start = PairShotSpacing.iconTextGap),
                            )
                        }
                    }
                }
            }
        },
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
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PairShotSpacing.screenPadding),
                        horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap),
                    ) {
                        FilterChip(
                            selected = !showCombinedOnly,
                            onClick = { if (showCombinedOnly) viewModel.toggleFilter() },
                            label = { Text(text = "전체 (${state.pairs.size})") },
                        )
                        FilterChip(
                            selected = showCombinedOnly,
                            onClick = { if (!showCombinedOnly) viewModel.toggleFilter() },
                            label = { Text(text = "합성 (${state.combinedCount})") },
                        )
                    }

                    if (displayedPairs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "촬영된 사진이 없습니다",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(PairShotSpacing.screenPadding),
                            horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.itemGap),
                            verticalArrangement = Arrangement.spacedBy(PairShotSpacing.itemGap),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(items = displayedPairs, key = { it.id }) { pair ->
                                if (showCombinedOnly) {
                                    CombinedCard(
                                        pair = pair,
                                        selectionMode = selectionMode,
                                        isSelected = pair.id in selectedIds,
                                        onClick = {
                                            if (selectionMode) {
                                                viewModel.toggleSelection(pair.id)
                                            } else {
                                                onNavigateToCompare(pair.id)
                                            }
                                        },
                                        onLongClick = { viewModel.longPressSelect(pair.id) },
                                    )
                                } else {
                                    PairCard(
                                        pair = pair,
                                        selectionMode = selectionMode,
                                        isSelected = pair.id in selectedIds,
                                        onClick = {
                                            if (selectionMode) {
                                                viewModel.toggleSelection(pair.id)
                                            } else {
                                                when (pair.status) {
                                                    PairStatus.BEFORE_ONLY -> onNavigateToPairing(pair.id)

                                                    PairStatus.PAIRED,
                                                    PairStatus.COMBINED,
                                                    -> onNavigateToCompare(pair.id)
                                                }
                                            }
                                        },
                                        onLongClick = { viewModel.longPressSelect(pair.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "선택 항목 삭제") },
            text = { Text(text = "${selectedIds.size}개 페어를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSelected()
                    },
                ) {
                    Text(
                        text = "삭제",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "취소")
                }
            },
        )
    }

    combineProgress?.let { progress ->
        AlertDialog(
            onDismissRequest = { },
            title = { Text(text = "합성 중") },
            text = {
                Column {
                    LinearProgressIndicator(
                        progress = { progress.current.toFloat() / progress.total },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                    Text(
                        text = "${progress.current}/${progress.total} 처리 중...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            confirmButton = { },
        )
    }
}
