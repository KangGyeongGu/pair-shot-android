package com.pairshot.feature.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.model.Album

@Composable
fun HomeAlbumGridSection(
    albums: List<Album>,
    isSelectionMode: Boolean,
    selectedAlbumIds: Set<Long>,
    onAlbumClick: (Long) -> Unit,
    onAlbumLongPress: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(PairShotSpacing.cardPadding),
    ) {
        item(key = "albums_group") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column {
                    albums.forEachIndexed { index, album ->
                        AlbumCard(
                            album = album,
                            isSelectionMode = isSelectionMode,
                            isSelected = album.id in selectedAlbumIds,
                            isFirst = index == 0,
                            isLast = index == albums.lastIndex,
                            onClick = { onAlbumClick(album.id) },
                            onLongPress = { onAlbumLongPress(album.id) },
                        )
                        if (index < albums.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = PairShotSpacing.cardPadding),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
