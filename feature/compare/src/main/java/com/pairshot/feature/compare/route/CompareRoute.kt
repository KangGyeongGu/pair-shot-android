package com.pairshot.feature.compare.route

import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.core.ui.component.PairShotSnackbarController
import com.pairshot.feature.compare.screen.CompareScreen
import com.pairshot.feature.compare.viewmodel.CompareViewModel

@Composable
fun CompareRoute(
    onNavigateBack: () -> Unit,
    onNavigateToAfterCamera: (projectId: Long, pairId: Long) -> Unit,
    viewModel: CompareViewModel = hiltViewModel(),
) {
    val pair by viewModel.pair.collectAsStateWithLifecycle()
    val pairNumber by viewModel.pairNumber.collectAsStateWithLifecycle()
    val pairs by viewModel.pairs.collectAsStateWithLifecycle()
    val currentPairId by viewModel.currentPairId.collectAsStateWithLifecycle()
    val isCombining by viewModel.isCombining.collectAsStateWithLifecycle()

    val snackbarController = remember { PairShotSnackbarController() }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.deleteComplete.collect { onNavigateBack() }
    }

    LaunchedEffect(Unit) {
        viewModel.retakeReady.collect { selectedPair ->
            onNavigateToAfterCamera(selectedPair.projectId, selectedPair.id)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.combineComplete.collect { event ->
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            val result = snackbarController.show(event)
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.combinePair()
            }
        }
    }

    CompareScreen(
        pair = pair,
        pairNumber = pairNumber,
        pairs = pairs,
        currentPairId = currentPairId,
        isCombining = isCombining,
        snackbarController = snackbarController,
        onNavigateBack = onNavigateBack,
        onSelectPair = viewModel::selectPair,
        onCombinePair = viewModel::combinePair,
        onPrepareRetake = viewModel::prepareRetake,
        onDeletePair = viewModel::deletePair,
    )
}
