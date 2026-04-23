package com.pairshot.feature.album.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.model.SortOrder
import com.pairshot.core.ui.component.PairCard

@Composable
fun AlbumPairGridSection(
    pairs: List<PhotoPair>,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    sortOrder: SortOrder,
    onPairClick: (Long) -> Unit,
    onPairLongPress: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val sortedPairs =
        when (sortOrder) {
            SortOrder.DESC -> pairs.sortedByDescending { it.beforeTimestamp }
            SortOrder.ASC -> pairs.sortedBy { it.beforeTimestamp }
        }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = sortedPairs, key = { it.id }) { pair ->
            PairCard(
                pair = pair,
                isSelected = pair.id in selectedIds,
                isSelectionMode = isSelectionMode,
                onClick = { onPairClick(pair.id) },
                onLongPress = { onPairLongPress(pair.id) },
            )
        }
    }
}
