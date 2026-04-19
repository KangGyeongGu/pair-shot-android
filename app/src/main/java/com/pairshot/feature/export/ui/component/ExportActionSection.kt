package com.pairshot.feature.export.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.pairshot.core.ui.component.PairShotActionBar
import com.pairshot.core.ui.component.PairShotActionBarItem

@Composable
internal fun ExportActionSection(
    onSaveToDevice: () -> Unit,
    onShare: () -> Unit,
    enabled: Boolean = true,
) {
    PairShotActionBar {
        PairShotActionBarItem(
            label = "공유",
            onClick = onShare,
            enabled = enabled,
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "공유")
        }
        PairShotActionBarItem(
            label = "기기에 저장",
            onClick = onSaveToDevice,
            enabled = enabled,
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "기기에 저장")
        }
    }
}
