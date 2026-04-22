package com.pairshot.feature.album.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pairshot.core.ui.component.PairShotActionBar
import com.pairshot.core.ui.component.PairShotActionBarItem

@Composable
fun AlbumSelectionBottomBar(
    onShareClick: () -> Unit,
    onSaveToDeviceClick: () -> Unit,
    onRemoveFromAlbumClick: () -> Unit,
    onExportSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PairShotActionBar {
        PairShotActionBarItem(
            label = "공유",
            onClick = onShareClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "공유",
                )
            },
        )
        PairShotActionBarItem(
            label = "기기저장",
            onClick = onSaveToDeviceClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.FileDownload,
                    contentDescription = "기기저장",
                )
            },
        )
        PairShotActionBarItem(
            label = "앨범에서 제거",
            onClick = onRemoveFromAlbumClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "앨범에서 제거",
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            labelColor = MaterialTheme.colorScheme.error,
        )
        PairShotActionBarItem(
            label = "내보내기 설정",
            onClick = onExportSettingsClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "내보내기 설정",
                )
            },
        )
    }
}
