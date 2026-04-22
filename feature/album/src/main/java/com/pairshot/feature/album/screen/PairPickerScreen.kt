package com.pairshot.feature.album.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.feature.album.component.PairPickerGridSection
import com.pairshot.feature.album.viewmodel.PairPickerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairPickerScreen(
    uiState: PairPickerUiState,
    onToggle: (Long) -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val countLabel =
                        if (uiState.selectedIds.isNotEmpty()) {
                            "페어 선택 (${uiState.selectedIds.size})"
                        } else {
                            "페어 선택"
                        }
                    Text(
                        text = countLabel,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "닫기",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp,
            ) {
                Button(
                    onClick = onConfirm,
                    enabled = uiState.selectedIds.isNotEmpty() && !uiState.isConfirming,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Text(text = if (uiState.isConfirming) "추가 중…" else "추가")
                }
            }
        },
    ) { innerPadding ->
        PairPickerGridSection(
            pairs = uiState.pairs,
            selectedIds = uiState.selectedIds,
            alreadyInAlbumIds = uiState.alreadyInAlbumIds,
            onToggle = onToggle,
            contentPadding =
                androidx.compose.foundation.layout.PaddingValues(
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    bottom = innerPadding.calculateBottomPadding() + 12.dp,
                    start = 12.dp,
                    end = 12.dp,
                ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
