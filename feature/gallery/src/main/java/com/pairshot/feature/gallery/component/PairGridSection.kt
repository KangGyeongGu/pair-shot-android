package com.pairshot.feature.gallery.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.model.PairStatus
import com.pairshot.core.model.PhotoPair
import com.pairshot.feature.gallery.component.PairCard

@Composable
internal fun PairGridSection(
    pairs: List<PhotoPair>,
    showCombinedOnly: Boolean,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onToggleSelection: (Long) -> Unit,
    onLongPressSelect: (Long) -> Unit,
    onNavigateToAfterCamera: (Long) -> Unit,
    onNavigateToCompare: (Long) -> Unit,
    onNavigateToCombined: (Long) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    if (pairs.isEmpty()) {
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
            contentPadding =
                PaddingValues(
                    start = PairShotSpacing.screenPadding,
                    top = PairShotSpacing.screenPadding,
                    end = PairShotSpacing.screenPadding,
                    bottom = if (selectionMode) PairShotSpacing.screenPadding else PairShotSpacing.fabOffset,
                ),
            horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.itemGap),
            verticalArrangement = Arrangement.spacedBy(PairShotSpacing.itemGap),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items = pairs, key = { it.id }) { pair ->
                PairCard(
                    pair = pair,
                    selectionMode = selectionMode,
                    isSelected = pair.id in selectedIds,
                    onClick = {
                        if (selectionMode) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleSelection(pair.id)
                        } else {
                            when {
                                showCombinedOnly -> onNavigateToCombined(pair.id)
                                pair.status == PairStatus.BEFORE_ONLY -> onNavigateToAfterCamera(pair.id)
                                else -> onNavigateToCompare(pair.id)
                            }
                        }
                    },
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPressSelect(pair.id)
                    },
                )
            }
        }
    }
}
