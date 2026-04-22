package com.pairshot.feature.home.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pairshot.core.ui.component.PairShotActionBar
import com.pairshot.core.ui.component.PairShotActionBarItem

@Composable
fun HomeAlbumSelectionBottomBar(
    selectedCount: Int,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PairShotActionBar {
        PairShotActionBarItem(
            label = "이름 수정",
            onClick = onRename,
            enabled = selectedCount == 1,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "이름 수정",
                )
            },
        )
        PairShotActionBarItem(
            label = "삭제",
            onClick = onDelete,
            enabled = selectedCount >= 1,
            labelColor = MaterialTheme.colorScheme.error,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.error,
                )
            },
        )
    }
}
