package com.pairshot.core.ui.component

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Precision
import kotlin.math.max
import kotlin.math.roundToInt

private const val CACHE_BUCKET_PX = 64

enum class ImageProfile(
    val cacheKey: String,
    val oversample: Float,
    val maxLongEdgePx: Int? = null,
) {
    THUMBNAIL(cacheKey = "thumbnail", oversample = 5.0f, maxLongEdgePx = 2048),
    DETAIL(cacheKey = "detail", oversample = 2.5f, maxLongEdgePx = 2304),
}

@Composable
fun ProfiledAsyncImage(
    data: Any?,
    profile: ImageProfile,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var slotSize by remember { mutableStateOf(IntSize.Zero) }
    val request =
        remember(context, data, slotSize, profile) {
            buildProfiledRequest(
                context = context,
                data = data,
                slotSize = slotSize,
                profile = profile,
            )
        }

    var isError by remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier.onSizeChanged { size ->
                if (size.width > 0 && size.height > 0 && size != slotSize) {
                    slotSize = size
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (request != null) {
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onState = { state ->
                    when (state) {
                        is AsyncImagePainter.State.Loading -> {
                            isError = false
                        }

                        is AsyncImagePainter.State.Error -> {
                            isError = true
                        }

                        else -> {}
                    }
                },
            )
        }
        if (isError) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "파일 없음",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

private fun buildProfiledRequest(
    context: Context,
    data: Any?,
    slotSize: IntSize,
    profile: ImageProfile,
): ImageRequest? {
    if (data == null || slotSize.width <= 0 || slotSize.height <= 0) {
        return null
    }

    var requestedWidth = (slotSize.width * profile.oversample).roundToInt().coerceAtLeast(1)
    var requestedHeight = (slotSize.height * profile.oversample).roundToInt().coerceAtLeast(1)

    profile.maxLongEdgePx?.let { maxLongEdge ->
        val currentLongEdge = max(requestedWidth, requestedHeight)
        if (currentLongEdge > maxLongEdge) {
            val ratio = maxLongEdge.toFloat() / currentLongEdge
            requestedWidth = (requestedWidth * ratio).roundToInt().coerceAtLeast(1)
            requestedHeight = (requestedHeight * ratio).roundToInt().coerceAtLeast(1)
        }
    }

    val bucketedWidth = toBucketSize(requestedWidth)
    val bucketedHeight = toBucketSize(requestedHeight)

    return ImageRequest
        .Builder(context)
        .data(data)
        .size(bucketedWidth, bucketedHeight)
        .precision(Precision.EXACT)
        .memoryCacheKeyExtra("profile", profile.cacheKey)
        .memoryCacheKeyExtra("bucket", "${bucketedWidth}x$bucketedHeight")
        .build()
}

private fun toBucketSize(value: Int): Int {
    val remainder = value % CACHE_BUCKET_PX
    return if (remainder == 0) value else value + (CACHE_BUCKET_PX - remainder)
}
