package com.pairshot.feature.album.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.feature.album.screen.PairPickerScreen
import com.pairshot.feature.album.viewmodel.PairPickerEvent
import com.pairshot.feature.album.viewmodel.PairPickerViewModel

@Composable
fun PairPickerRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PairPickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is PairPickerEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    PairPickerScreen(
        uiState = uiState,
        onToggle = viewModel::toggleSelection,
        onConfirm = viewModel::confirmSelection,
        onClose = onNavigateBack,
        modifier = modifier,
    )
}
