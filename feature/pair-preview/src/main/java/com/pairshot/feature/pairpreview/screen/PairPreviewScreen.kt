package com.pairshot.feature.pairpreview.screen

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pairshot.core.model.CombineHistory
import com.pairshot.core.ui.component.DeletePairConfirmDialog
import com.pairshot.feature.pairpreview.component.PairPreviewCenter
import com.pairshot.feature.pairpreview.component.PairPreviewTopBar

@Composable
fun PairPreviewScreen(
    combined: CombineHistory?,
    livePreviewBitmap: Bitmap?,
    showDeleteDialog: Boolean,
    onClose: () -> Unit,
    onShareSelected: () -> Unit,
    onNavigateToAfterCamera: () -> Unit,
    onDeleteRequested: () -> Unit,
    onDeleteAll: () -> Unit,
    onDeleteCombinedOnly: () -> Unit,
    onDeleteDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding(),
    ) {
        PairPreviewTopBar(
            onClose = onClose,
            onShareSelected = onShareSelected,
            onNavigateToAfterCamera = onNavigateToAfterCamera,
            onDeleteRequested = onDeleteRequested,
            modifier = Modifier.fillMaxWidth(),
        )

        Box(modifier = Modifier.weight(1f)) {
            PairPreviewCenter(
                combined = combined,
                livePreviewBitmap = livePreviewBitmap,
            )
        }
    }

    if (showDeleteDialog) {
        DeletePairConfirmDialog(
            pairCount = 1,
            combinedCount = if (combined != null) 1 else 0,
            onDeleteAll = onDeleteAll,
            onDeleteCombinedOnly = onDeleteCombinedOnly,
            onDismiss = onDeleteDismissed,
        )
    }
}
