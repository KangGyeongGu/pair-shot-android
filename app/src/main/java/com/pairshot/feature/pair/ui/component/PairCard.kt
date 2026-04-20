package com.pairshot.feature.pair.ui.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.domain.pair.PairStatus
import com.pairshot.core.domain.pair.PhotoPair
import com.pairshot.core.ui.component.ImageProfile
import com.pairshot.core.ui.component.ProfiledAsyncImage

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
        if (selectionMode && isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            .padding(end = PairShotSpacing.iconTextGap, top = PairShotSpacing.iconTextGap),
                )
            }
        }
    }
}

@Composable
private fun CombinedStatusBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(28.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Icon(
            imageVector = Icons.Filled.JoinRight,
            contentDescription = "합성 완료",
            modifier = Modifier.padding(4.dp).size(20.dp),
        )
    }
}
