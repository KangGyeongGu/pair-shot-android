package com.pairshot.feature.home.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pairshot.core.ui.component.PairShotActionBar
import com.pairshot.core.ui.component.PairShotActionBarItem

@Composable
fun HomeSelectionBottomBar(
    selectedCount: Int,
    onShare: () -> Unit,
    onSaveToDevice: () -> Unit,
    onDelete: () -> Unit,
    onExportSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasSelection = selectedCount > 0

    PairShotActionBar {
        PairShotActionBarItem(
            label = "공유",
            onClick = onShare,
            enabled = hasSelection,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "공유",
                    tint =
                        if (hasSelection) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                )
            },
        )
        PairShotActionBarItem(
            label = "기기저장",
            onClick = onSaveToDevice,
            enabled = hasSelection,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = "기기저장",
                    tint =
                        if (hasSelection) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                )
            },
        )
        PairShotActionBarItem(
            label = "삭제",
            onClick = onDelete,
            enabled = hasSelection,
            labelColor = MaterialTheme.colorScheme.error,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "삭제",
                    tint =
                        if (hasSelection) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
                        },
                )
            },
        )
        PairShotActionBarItem(
            label = "내보내기설정",
            onClick = onExportSettings,
            enabled = hasSelection,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "내보내기설정",
                    tint =
                        if (hasSelection) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                )
            },
        )
    }
}
