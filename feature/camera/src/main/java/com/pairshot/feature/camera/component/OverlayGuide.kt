package com.pairshot.feature.camera.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun OverlayGuide(
    bitmap: Bitmap?,
    rotation: Float,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    if (bitmap == null || alpha <= 0f) return
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier =
            modifier
                .rotate(rotation)
                .alpha(alpha),
    )
}
