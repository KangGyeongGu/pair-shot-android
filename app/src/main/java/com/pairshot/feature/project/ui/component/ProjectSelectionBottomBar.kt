package com.pairshot.feature.project.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.pairshot.core.ui.component.PairShotActionBar
import com.pairshot.core.ui.component.PairShotActionBarItem

@Composable
internal fun ProjectSelectionBottomBar(
    selectedCount: Int,
    onShowRenameDialog: () -> Unit,
    onShowDeleteDialog: () -> Unit,
) {
    val editEnabled = selectedCount == 1
    val deleteEnabled = selectedCount > 0
    val deleteColor = if (deleteEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
    PairShotActionBar {
        // 수정: 아이콘 자체가 Edit/EditOff로 교체되므로 tint는 IconButton 기본 disabled 처리에 위임
        PairShotActionBarItem(
            label = "수정",
            onClick = onShowRenameDialog,
            enabled = editEnabled,
        ) {
            Icon(
                imageVector = if (editEnabled) Icons.Default.Edit else Icons.Default.EditOff,
                contentDescription = "이름 변경",
            )
        }
        // 삭제: error 색상이므로 비활성 alpha를 명시적으로 처리
        PairShotActionBarItem(
            label = "삭제",
            onClick = onShowDeleteDialog,
            enabled = deleteEnabled,
            labelColor = deleteColor,
        ) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = "선택 삭제",
                tint = deleteColor,
            )
        }
    }
}
