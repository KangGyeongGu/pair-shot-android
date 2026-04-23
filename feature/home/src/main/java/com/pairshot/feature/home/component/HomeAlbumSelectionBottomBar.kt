package com.pairshot.feature.home.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pairshot.core.ui.component.PairShotActionBar
import com.pairshot.core.ui.component.PairShotActionBarItem
import com.pairshot.feature.home.R
import com.pairshot.core.ui.R as CoreR

@Composable
fun HomeAlbumSelectionBottomBar(
    selectedCount: Int,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PairShotActionBar {
        PairShotActionBarItem(
            label = stringResource(R.string.home_button_album_rename),
            onClick = onRename,
            enabled = selectedCount == 1,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.home_button_album_rename),
                )
            },
        )
        PairShotActionBarItem(
            label = stringResource(CoreR.string.common_button_delete),
            onClick = onDelete,
            enabled = selectedCount >= 1,
            labelColor = MaterialTheme.colorScheme.error,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(CoreR.string.common_button_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            },
        )
    }
}
