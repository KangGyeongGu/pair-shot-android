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
import androidx.compose.ui.res.stringResource
import com.pairshot.core.ui.component.PairShotActionBar
import com.pairshot.core.ui.component.PairShotActionBarItem
import com.pairshot.feature.album.R
import com.pairshot.core.ui.R as CoreR

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
            label = stringResource(CoreR.string.common_button_share),
            onClick = onShareClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(CoreR.string.common_button_share),
                )
            },
        )
        PairShotActionBarItem(
            label = stringResource(CoreR.string.common_button_save_to_device),
            onClick = onSaveToDeviceClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.FileDownload,
                    contentDescription = stringResource(CoreR.string.common_button_save_to_device),
                )
            },
        )
        PairShotActionBarItem(
            label = stringResource(R.string.album_button_remove_from_album),
            onClick = onRemoveFromAlbumClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.album_button_remove_from_album),
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            labelColor = MaterialTheme.colorScheme.error,
        )
        PairShotActionBarItem(
            label = stringResource(R.string.album_button_export_settings_long),
            onClick = onExportSettingsClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.album_button_export_settings_long),
                )
            },
        )
    }
}
