package com.pairshot.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest

@Composable
fun OverlayGuide(
    imageUri: String?,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    if (imageUri == null) return

    AsyncImage(
        model =
            ImageRequest
                .Builder(LocalContext.current)
                .data(imageUri)
                .build(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.alpha(alpha),
    )
}
