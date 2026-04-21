package com.pairshot.feature.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pairshot.core.ui.component.ImageProfile
import com.pairshot.core.ui.component.ProfiledAsyncImage

private val DefaultStripHeight = 120.dp

@Composable
fun BeforePreviewStrip(
    beforePreviewUris: List<String>,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
    onSelectIndex: ((Int) -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    emptyMessage: String = "아직 촬영된 Before가 없습니다",
    stripHeight: Dp = DefaultStripHeight,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(stripHeight)
                .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (beforePreviewUris.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
            )
        } else {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(
                    items = beforePreviewUris,
                    key = { index, uri -> "$uri-$index" },
                ) { index, beforeUri ->
                    val isSelected = selectedIndex == index
                    val borderWidth = if (isSelected) 2.dp else 1.dp
                    val borderColor =
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        ProfiledAsyncImage(
                            data = beforeUri,
                            profile = ImageProfile.THUMBNAIL,
                            contentDescription = "Before 썸네일 ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier =
                                Modifier
                                    .size(width = 72.dp, height = 96.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = borderWidth,
                                        color = borderColor,
                                        shape = RoundedCornerShape(8.dp),
                                    ).then(
                                        if (onSelectIndex != null) {
                                            Modifier.clickable { onSelectIndex(index) }
                                        } else {
                                            Modifier
                                        },
                                    ),
                        )
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
