package com.pairshot.feature.pair.ui.component

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
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.domain.pair.PairStatus
import com.pairshot.core.domain.pair.PhotoPair
import com.pairshot.feature.pair.ui.component.CombinedCard
import com.pairshot.feature.pair.ui.component.PairCard

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
) {
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
                    bottom = if (selectionMode) PairShotSpacing.screenPadding else 96.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.itemGap),
            verticalArrangement = Arrangement.spacedBy(PairShotSpacing.itemGap),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items = pairs, key = { it.id }) { pair ->
                if (showCombinedOnly) {
                    CombinedCard(
                        pair = pair,
                        selectionMode = selectionMode,
                        isSelected = pair.id in selectedIds,
                        onClick = {
                            if (selectionMode) {
                                onToggleSelection(pair.id)
                            } else {
                                onNavigateToCompare(pair.id)
                            }
                        },
                        onLongClick = { onLongPressSelect(pair.id) },
                    )
                } else {
                    PairCard(
                        pair = pair,
                        selectionMode = selectionMode,
                        isSelected = pair.id in selectedIds,
                        onClick = {
                            if (selectionMode) {
                                onToggleSelection(pair.id)
                            } else {
                                when (pair.status) {
                                    PairStatus.BEFORE_ONLY -> onNavigateToAfterCamera(pair.id)

                                    PairStatus.PAIRED,
                                    PairStatus.COMBINED,
                                    -> onNavigateToCompare(pair.id)
                                }
                            }
                        },
                        onLongClick = { onLongPressSelect(pair.id) },
                    )
                }
            }
        }
    }
}
