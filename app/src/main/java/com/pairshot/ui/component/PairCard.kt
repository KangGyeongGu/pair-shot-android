package com.pairshot.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import com.pairshot.domain.model.PairStatus
import com.pairshot.domain.model.PhotoPair
import com.pairshot.ui.theme.Warning

@Composable
fun PairCard(
    pair: PhotoPair,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor =
        when (pair.status) {
            PairStatus.BEFORE_ONLY -> Warning

            PairStatus.PAIRED,
            PairStatus.COMBINED,
            -> MaterialTheme.colorScheme.primary
        }

    val context = LocalContext.current

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, borderColor),
    ) {
        Box {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Before image
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(context)
                            .data(pair.beforePhotoUri)
                            .size(Size.ORIGINAL)
                            .build(),
                    contentDescription = "Before 사진",
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .weight(1f)
                            .aspectRatio(3f / 4f),
                )

                Spacer(modifier = Modifier.width(2.dp))

                // After slot
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

                    PairStatus.PAIRED -> {
                        AsyncImage(
                            model =
                                ImageRequest
                                    .Builder(context)
                                    .data(pair.afterPhotoUri)
                                    .size(Size.ORIGINAL)
                                    .build(),
                            contentDescription = "After 사진",
                            contentScale = ContentScale.Fit,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(3f / 4f),
                        )
                    }

                    PairStatus.COMBINED -> {
                        AsyncImage(
                            model =
                                ImageRequest
                                    .Builder(context)
                                    .data(pair.combinedPhotoUri ?: pair.afterPhotoUri)
                                    .size(Size.ORIGINAL)
                                    .build(),
                            contentDescription = "합성 사진",
                            contentScale = ContentScale.Fit,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(3f / 4f),
                        )
                    }
                }
            }

            // "✓ 합성됨" overlay — COMBINED only
            if (pair.status == PairStatus.COMBINED) {
                Text(
                    text = "✓ 합성됨",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 8.dp, top = 6.dp),
                )
            }
        }
    }
}
