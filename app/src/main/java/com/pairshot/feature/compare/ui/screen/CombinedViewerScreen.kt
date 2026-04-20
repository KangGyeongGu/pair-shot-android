package com.pairshot.feature.compare.ui.screen

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.domain.pair.PhotoPair
import com.pairshot.core.ui.component.ImageProfile
import com.pairshot.core.ui.component.ProfiledAsyncImage
import com.pairshot.feature.compare.ui.component.CompareModalScaffold
import com.pairshot.feature.compare.ui.component.ComparePagerControls

private val HeaderHeight = 44.dp
private const val SwipeThreshold = 72f

@Composable
fun CombinedViewerScreen(
    pair: PhotoPair?,
    pairs: List<PhotoPair>,
    currentPairId: Long,
    pairNumber: Int,
    onSelectPair: (Long) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val currentIndex = pairs.indexOfFirst { it.id == currentPairId }
    val canGoPrev = currentIndex > 0
    val canGoNext = currentIndex in 0 until pairs.lastIndex

    CompareModalScaffold(onDismiss = onNavigateBack) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PairShotSpacing.itemGap, vertical = PairShotSpacing.itemGap),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(HeaderHeight)
                        .padding(horizontal = PairShotSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "합성 이미지",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.size(PairShotSpacing.iconTextGap))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PairShotSpacing.xs)
                        .pointerInput(pairs, currentPairId) {
                            var dragTotal = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { dragTotal = 0f },
                                onHorizontalDrag = { _, dragAmount ->
                                    dragTotal += dragAmount
                                },
                                onDragEnd = {
                                    when {
                                        dragTotal > SwipeThreshold && canGoPrev -> {
                                            onSelectPair(pairs[currentIndex - 1].id)
                                        }

                                        dragTotal < -SwipeThreshold && canGoNext -> {
                                            onSelectPair(pairs[currentIndex + 1].id)
                                        }
                                    }
                                    dragTotal = 0f
                                },
                                onDragCancel = { dragTotal = 0f },
                            )
                        },
            ) {
                ProfiledAsyncImage(
                    data = pair?.combinedPhotoUri,
                    profile = ImageProfile.DETAIL,
                    contentDescription = "합성 이미지",
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 2f),
                )
            }

            ComparePagerControls(
                pairNumber = pairNumber,
                pairsSize = pairs.size,
                canGoPrev = canGoPrev,
                canGoNext = canGoNext,
                onPrev = { if (canGoPrev) onSelectPair(pairs[currentIndex - 1].id) },
                onNext = { if (canGoNext) onSelectPair(pairs[currentIndex + 1].id) },
            )
        }
    }
}
