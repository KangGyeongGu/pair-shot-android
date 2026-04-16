package com.pairshot.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.domain.model.PairStatus
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
    onNavigateToExport: () -> Unit = {},
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showCombinedOnly by viewModel.showCombinedOnly.collectAsStateWithLifecycle()

    val projectName =
        when (val state = uiState) {
            is GalleryUiState.Success -> state.projectName
            else -> "갤러리"
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = projectName,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { /* stub */ }) {
                        Text(text = "선택")
                    }
                    IconButton(onClick = onNavigateToExport) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더보기",
                        )
                    }
                },
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
                                PairCard(
                                    pair = pair,
                                    onClick = {
                                        when (pair.status) {
                                            PairStatus.BEFORE_ONLY -> onNavigateToPairing(pair.id)

                                            PairStatus.PAIRED,
                                            PairStatus.COMBINED,
                                            -> onNavigateToCompare(pair.id)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
