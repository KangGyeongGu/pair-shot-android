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
import com.pairshot.core.ui.component.PairCard

@Composable
fun AlbumPairGridSection(
    pairs: List<PhotoPair>,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    onPairClick: (Long) -> Unit,
    onPairLongPress: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = pairs, key = { it.id }) { pair ->
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
