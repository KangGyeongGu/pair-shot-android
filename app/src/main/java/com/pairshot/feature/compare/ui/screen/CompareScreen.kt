package com.pairshot.feature.compare.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.domain.model.PhotoPair
import com.pairshot.feature.compare.ui.component.CompareHeader
import com.pairshot.feature.compare.ui.component.CompareImagePane
import com.pairshot.feature.compare.ui.component.CompareModalScaffold
import com.pairshot.feature.compare.ui.component.ComparePagerControls
import com.pairshot.feature.compare.ui.dialog.CombiningDialog
import com.pairshot.feature.compare.ui.dialog.DeleteConfirmDialog

@Composable
fun CompareScreen(
    pair: PhotoPair?,
    pairNumber: Int,
    pairs: List<PhotoPair>,
    currentPairId: Long,
    isCombining: Boolean,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onSelectPair: (Long) -> Unit,
    onCombinePair: () -> Unit,
    onPrepareRetake: () -> Unit,
    onDeletePair: () -> Unit,
) {
    val currentIndex = pairs.indexOfFirst { it.id == currentPairId }
    val canGoPrev = currentIndex > 0
    val canGoNext = currentIndex in 0 until pairs.lastIndex

    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    CompareModalScaffold(onDismiss = onNavigateBack) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            CompareHeader(
                pairNumber = pairNumber,
                pairsSize = pairs.size,
                pairStatus = pair?.status,
                isCombining = isCombining,
                menuExpanded = menuExpanded,
                onMenuExpandedChange = { menuExpanded = it },
                onNavigateBack = onNavigateBack,
                onCombinePair = onCombinePair,
                onPrepareRetake = onPrepareRetake,
                onShowDeleteDialog = { showDeleteDialog = true },
            )

            Spacer(modifier = Modifier.size(6.dp))

            CompareImagePane(
                pair = pair,
                pairs = pairs,
                currentPairId = currentPairId,
                canGoPrev = canGoPrev,
                canGoNext = canGoNext,
                currentIndex = currentIndex,
                onSelectPair = onSelectPair,
            )

            ComparePagerControls(
                pairNumber = pairNumber,
                pairsSize = pairs.size,
                canGoPrev = canGoPrev,
                canGoNext = canGoNext,
                onPrev = {
                    if (canGoPrev) onSelectPair(pairs[currentIndex - 1].id)
                },
                onNext = {
                    if (canGoNext) onSelectPair(pairs[currentIndex + 1].id)
                },
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 0.dp),
            )
        }
    }

    if (isCombining) {
        CombiningDialog()
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                onDeletePair()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}
