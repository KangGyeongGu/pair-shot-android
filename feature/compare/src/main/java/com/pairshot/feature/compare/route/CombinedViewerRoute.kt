package com.pairshot.feature.compare.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.feature.compare.screen.CombinedViewerScreen
import com.pairshot.feature.compare.viewmodel.CombinedViewerViewModel

@Composable
fun CombinedViewerRoute(
    onNavigateBack: () -> Unit,
    viewModel: CombinedViewerViewModel = hiltViewModel(),
) {
    val pair by viewModel.pair.collectAsStateWithLifecycle()
    val pairs by viewModel.pairs.collectAsStateWithLifecycle()
    val currentPairId by viewModel.currentPairId.collectAsStateWithLifecycle()
    val pairNumber by viewModel.pairNumber.collectAsStateWithLifecycle()

    CombinedViewerScreen(
        pair = pair,
        pairs = pairs,
        currentPairId = currentPairId,
        pairNumber = pairNumber,
        onSelectPair = viewModel::selectPair,
        onNavigateBack = onNavigateBack,
    )
}
