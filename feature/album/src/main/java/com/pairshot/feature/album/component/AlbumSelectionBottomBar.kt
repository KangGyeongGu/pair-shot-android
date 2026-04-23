package com.pairshot.feature.album.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pairshot.core.ui.component.PairShotActionBar
import com.pairshot.core.ui.component.PairShotActionBarItem
import com.pairshot.core.ui.R as CoreR

@Composable
fun AlbumSelectionBottomBar(
    onShareClick: () -> Unit,
    onSaveToDeviceClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PairShotActionBar {
        PairShotActionBarItem(
            label = stringResource(CoreR.string.common_button_share),
            onClick = onShareClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = stringResource(CoreR.string.common_button_share),
                )
            },
        )
        PairShotActionBarItem(
            label = stringResource(CoreR.string.common_button_save_to_device),
            onClick = onSaveToDeviceClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = stringResource(CoreR.string.common_button_save_to_device),
                )
            },
        )
        PairShotActionBarItem(
            label = stringResource(CoreR.string.common_button_delete),
            onClick = onDeleteClick,
            labelColor = MaterialTheme.colorScheme.error,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(CoreR.string.common_button_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            },
        )
        PairShotActionBarItem(
            label = stringResource(CoreR.string.common_button_export_settings),
            onClick = onExportSettingsClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = stringResource(CoreR.string.common_button_export_settings),
                )
            },
        )
    }
}
