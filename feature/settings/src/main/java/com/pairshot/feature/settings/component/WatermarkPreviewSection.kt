package com.pairshot.feature.settings.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.rendering.PreviewSampleProvider
import com.pairshot.core.rendering.WatermarkRenderer
import com.pairshot.feature.settings.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREVIEW_SCALE_FACTOR = 0.5f
private const val PREVIEW_MIN_DIMENSION_PX = 200

@Composable
internal fun WatermarkPreviewSection(
    config: WatermarkConfig,
    watermarkRenderer: WatermarkRenderer,
    previewSampleProvider: PreviewSampleProvider,
    modifier: Modifier = Modifier,
) {
    val previewConfig =
        remember(config) {
            config.copy(enabled = true)
        }

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var aspectRatio by remember { mutableStateOf(1f) }

    LaunchedEffect(previewConfig) {
        val sample = previewSampleProvider.get()
        aspectRatio = sample.width.toFloat() / sample.height.toFloat().coerceAtLeast(1f)
        val result =
            withContext(Dispatchers.Default) {
                val scaled =
                    Bitmap.createScaledBitmap(
                        sample,
                        (sample.width * PREVIEW_SCALE_FACTOR).toInt().coerceAtLeast(PREVIEW_MIN_DIMENSION_PX),
                        (sample.height * PREVIEW_SCALE_FACTOR).toInt().coerceAtLeast(PREVIEW_MIN_DIMENSION_PX),
                        true,
                    )
                val applied = watermarkRenderer.apply(scaled, previewConfig)
                if (applied !== scaled) scaled.recycle()
                applied
            }
        previewBitmap?.let { old ->
            if (old !== result && !old.isRecycled) old.recycle()
        }
        previewBitmap = result
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        val bmp = previewBitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = stringResource(R.string.watermark_preview_desc),
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio),
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio),
            )
        }
    }
}
