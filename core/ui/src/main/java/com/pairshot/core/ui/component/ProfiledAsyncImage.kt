package com.pairshot.core.ui.component

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pairshot.core.ui.R

enum class ImageProfile {
    THUMBNAIL,
    DETAIL,
}

@Composable
fun ProfiledAsyncImage(
    data: Any?,
    profile: ImageProfile,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
) {
    val model =
        remember(data) {
            when (data) {
                is Uri -> data
                is String -> runCatching { Uri.parse(data) }.getOrNull()
                else -> null
            }
        }

    if (model == null) {
        ErrorPlaceholder(modifier)
        return
    }

    val context = LocalContext.current
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant.toArgb()

    val scaleType =
        when (contentScale) {
            ContentScale.Crop -> ImageView.ScaleType.CENTER_CROP
            ContentScale.Fit, ContentScale.Inside -> ImageView.ScaleType.FIT_CENTER
            else -> ImageView.ScaleType.CENTER_CROP
        }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                ImageView(ctx).apply {
                    setBackgroundColor(placeholderColor)
                    this.scaleType = scaleType
                    this.contentDescription = contentDescription
                }
            },
            update = { view ->
                view.scaleType = scaleType
                Glide
                    .with(context)
                    .load(model)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            },
            onRelease = { view ->
                Glide.with(context).clear(view)
            },
        )
    }
}

@Composable
private fun ErrorPlaceholder(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        ErrorContent()
    }
}

@Composable
private fun ErrorContent() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.image_file_missing),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}
