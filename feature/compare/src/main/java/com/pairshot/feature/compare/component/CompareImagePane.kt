package com.pairshot.feature.compare.component

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.ui.component.ImageProfile
import com.pairshot.core.ui.component.ProfiledAsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ImagePaneVerticalPadding = 5.dp
private val ImageSeparatorWidth = 10.dp

@Composable
internal fun CompareImagePane(
    pair: PhotoPair?,
    pairs: List<PhotoPair>,
    currentPairId: Long,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    currentIndex: Int,
    onSelectPair: (Long) -> Unit,
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = PairShotSpacing.iconTextGap),
    ) {
        Text(
            text = "Before",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "After",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = PairShotSpacing.xs, vertical = ImagePaneVerticalPadding)
                .pointerInput(pairs, currentPairId) {
                    var dragTotal = 0f
                    val swipeThreshold = 72f
                    detectHorizontalDragGestures(
                        onDragStart = { dragTotal = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            dragTotal += dragAmount
                        },
                        onDragEnd = {
                            when {
                                dragTotal > swipeThreshold && canGoPrev -> {
                                    onSelectPair(pairs[currentIndex - 1].id)
                                }

                                dragTotal < -swipeThreshold && canGoNext -> {
                                    onSelectPair(pairs[currentIndex + 1].id)
                                }
                            }
                            dragTotal = 0f
                        },
                        onDragCancel = { dragTotal = 0f },
                    )
                },
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ProfiledAsyncImage(
                data = pair?.beforePhotoUri,
                profile = ImageProfile.DETAIL,
                contentDescription = "Before 사진",
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(3f / 4f),
            )
            Spacer(modifier = Modifier.width(ImageSeparatorWidth))
            ProfiledAsyncImage(
                data = pair?.afterPhotoUri,
                profile = ImageProfile.DETAIL,
                contentDescription = "After 사진",
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(3f / 4f),
            )
        }
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 1.dp),
    ) {
        Text(
            text =
                pair?.beforeTimestamp?.let {
                    "${fullDateFormat.format(Date(it))} ${timeFormat.format(Date(it))}"
                } ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        Text(
            text =
                pair?.afterTimestamp?.let {
                    "${fullDateFormat.format(Date(it))} ${timeFormat.format(Date(it))}"
                } ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
    }
}
