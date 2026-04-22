package com.pairshot.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.JoinRight
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pairshot.core.model.PairStatus
import com.pairshot.core.model.PhotoPair

private val SelectionBorderWidth = 2.dp
private val CombinedBadgeSize = 28.dp
private val CombinedBadgeIconSize = 20.dp
private val CombinedBadgeIconPadding = 4.dp
private val BadgeEdgePadding = 8.dp

@Composable
fun PairCard(
    pair: PhotoPair,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderModifier =
        if (isSelected) {
            Modifier.border(
                width = SelectionBorderWidth,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium,
            )
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clip(MaterialTheme.shapes.medium)
                .then(borderModifier)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress,
                ),
    ) {
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
                        contentDescription = "촬영 예정",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }
            }
        }

        if (pair.hasCombined) {
            CombinedStatusBadge(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = BadgeEdgePadding, top = BadgeEdgePadding),
            )
        }

        if (isSelectionMode && isSelected) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            )
        }
    }
}

@Composable
private fun CombinedStatusBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(CombinedBadgeSize),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Icon(
            imageVector = Icons.Filled.JoinRight,
            contentDescription = "합성 완료",
            modifier = Modifier.padding(CombinedBadgeIconPadding).size(CombinedBadgeIconSize),
        )
    }
}
