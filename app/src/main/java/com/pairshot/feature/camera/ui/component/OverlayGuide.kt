package com.pairshot.feature.camera.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun OverlayGuide(
    imageUri: String?,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    if (imageUri == null) return

    AsyncImage(
        model = imageUri,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.alpha(alpha),
    )
}
