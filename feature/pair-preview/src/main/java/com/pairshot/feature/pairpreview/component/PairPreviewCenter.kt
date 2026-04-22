package com.pairshot.feature.pairpreview.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.pairshot.core.model.CombineHistory
import com.pairshot.core.ui.component.ImageProfile
import com.pairshot.core.ui.component.ProfiledAsyncImage

@Composable
fun PairPreviewCenter(
    combined: CombineHistory?,
    livePreviewBitmap: Bitmap?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (combined != null) {
            SavedHistoryContent(uri = combined.mediaStoreUri)
        } else {
            LivePreviewContent(bitmap = livePreviewBitmap)
        }
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
            contentDescription = "합성 미리보기",
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SavedHistoryContent(
    uri: String,
    modifier: Modifier = Modifier,
) {
    ProfiledAsyncImage(
        data = uri,
        profile = ImageProfile.DETAIL,
        contentDescription = "저장된 합성 이미지",
        contentScale = ContentScale.Fit,
        modifier = modifier.fillMaxSize(),
    )
}
