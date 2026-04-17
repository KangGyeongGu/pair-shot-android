package com.pairshot.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.JoinRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pairshot.domain.model.PairStatus
import com.pairshot.domain.model.PhotoPair
import com.pairshot.ui.theme.Success

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PairCard(
    pair: PhotoPair,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
) {
    val borderStroke =
        when {
            selectionMode && isSelected -> {
                BorderStroke(2.dp, Success)
            }

            else -> {
                BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
            }
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = borderStroke,
    ) {
        Box {
            Row(modifier = Modifier.fillMaxWidth()) {
                ProfiledAsyncImage(
                    data = pair.beforePhotoUri,
                    profile = ImageProfile.THUMBNAIL,
                    contentDescription = "Before 사진",
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .weight(1f)
                            .aspectRatio(3f / 4f),
                )

                Spacer(modifier = Modifier.width(2.dp))

                when (pair.status) {
                    PairStatus.BEFORE_ONLY -> {
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(3f / 4f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "After",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }
                    }

                    PairStatus.PAIRED,
                    PairStatus.COMBINED,
                    -> {
                        ProfiledAsyncImage(
                            data = pair.afterPhotoUri,
                            profile = ImageProfile.THUMBNAIL,
                            contentDescription = "After 사진",
                            contentScale = ContentScale.Crop,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(3f / 4f),
                        )
                    }
                }
            }

            if (pair.status == PairStatus.COMBINED) {
                CombinedStatusBadge(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 8.dp, top = 6.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CombinedCard(
    pair: PhotoPair,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
) {
    val borderStroke =
        when {
            selectionMode && isSelected -> BorderStroke(2.dp, Success)
            selectionMode -> BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
            else -> BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary)
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = borderStroke,
    ) {
        ProfiledAsyncImage(
            data = pair.combinedPhotoUri,
            profile = ImageProfile.THUMBNAIL,
            contentDescription = "합성 사진",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 2f),
        )
    }
}

@Composable
private fun CombinedStatusBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = Success,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
    ) {
        Icon(
            imageVector = Icons.Filled.JoinRight,
            contentDescription = null,
            modifier = Modifier.padding(5.dp).size(24.dp),
        )
    }
}
