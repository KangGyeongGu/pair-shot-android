package com.pairshot.feature.pair.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.JoinRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.pairshot.core.ui.component.PairShotActionBar
import com.pairshot.core.ui.component.PairShotActionBarItem

@Composable
internal fun GallerySelectionBottomBar(
    selectedCount: Int,
    onCombineSelected: () -> Unit,
    onExportSelected: () -> Unit,
    onShowDeleteDialog: () -> Unit,
) {
    PairShotActionBar {
        PairShotActionBarItem(
            label = "합성",
            onClick = onCombineSelected,
            enabled = selectedCount > 0,
        ) {
            Icon(imageVector = Icons.Default.JoinRight, contentDescription = "합성")
        }
        PairShotActionBarItem(
            label = "공유",
            onClick = onExportSelected,
            enabled = selectedCount > 0,
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "공유")
        }
        val deleteEnabled = selectedCount > 0
        val deleteColor = if (deleteEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
        PairShotActionBarItem(
            label = "삭제",
            onClick = onShowDeleteDialog,
            enabled = deleteEnabled,
            labelColor = deleteColor,
        ) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = "삭제",
                tint = deleteColor,
            )
        }
    }
}
