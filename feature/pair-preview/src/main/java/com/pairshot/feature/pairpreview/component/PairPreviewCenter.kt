package com.pairshot.feature.pairpreview.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.pairshot.feature.pairpreview.R

private const val MinZoomScale = 1f
private const val MaxZoomScale = 4f

@Composable
fun PairPreviewCenter(
    livePreviewBitmap: Bitmap?,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(MinZoomScale) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(MinZoomScale, MaxZoomScale)
                        scale = newScale
                        offset =
                            if (newScale > MinZoomScale) {
                                offset + pan
                            } else {
                                Offset.Zero
                            }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        LivePreviewContent(
            bitmap = livePreviewBitmap,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    ),
        )
    }
}

@Composable
private fun LivePreviewContent(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
) {
    if (bitmap != null && !bitmap.isRecycled) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.pair_preview_desc_combined_preview),
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
