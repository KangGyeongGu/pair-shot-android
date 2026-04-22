package com.pairshot.feature.album.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pairshot.core.model.PairStatus
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.ui.component.ImageProfile
import com.pairshot.core.ui.component.ProfiledAsyncImage

@Composable
fun PairPickerGridSection(
    pairs: List<PhotoPair>,
    selectedIds: Set<Long>,
    alreadyInAlbumIds: Set<Long>,
    onToggle: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    if (pairs.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "페어가 없습니다",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = pairs, key = { it.id }) { pair ->
            PairPickerCard(
                pair = pair,
                isSelected = pair.id in selectedIds,
                isAlreadyInAlbum = pair.id in alreadyInAlbumIds,
                onToggle = { onToggle(pair.id) },
            )
        }
    }
}

@Composable
private fun PairPickerCard(
    pair: PhotoPair,
    isSelected: Boolean,
    isAlreadyInAlbum: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium
    val border =
        when {
            isAlreadyInAlbum -> Modifier.border(2.dp, MaterialTheme.colorScheme.outline, shape)
            isSelected -> Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
            else -> Modifier
        }

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clip(shape)
                .then(border)
                .clickable(
                    enabled = !isAlreadyInAlbum,
                    onClick = onToggle,
                ),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box {
            Row(modifier = Modifier.fillMaxSize()) {
                ProfiledAsyncImage(
                    data = pair.beforePhotoUri,
                    profile = ImageProfile.THUMBNAIL,
                    contentDescription = "Before",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.weight(1f).fillMaxSize(),
                )
                if (pair.status == PairStatus.PAIRED && pair.afterPhotoUri != null) {
                    ProfiledAsyncImage(
                        data = pair.afterPhotoUri,
                        profile = ImageProfile.THUMBNAIL,
                        contentDescription = "After",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        )
                    }
                }
            }

            if (isAlreadyInAlbum) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "이미 추가됨",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            } else if (isSelected) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                )
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(22.dp),
                )
            }
        }
    }
}
