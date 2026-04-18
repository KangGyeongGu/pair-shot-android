package com.pairshot.feature.compare.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.domain.pair.PairStatus
import com.pairshot.core.ui.component.PairShotTopMenu
import com.pairshot.core.ui.component.PairShotTopMenuDivider
import com.pairshot.core.ui.component.PairShotTopMenuItem

private val CompareHeaderHeight = 44.dp

@Composable
internal fun CompareHeader(
    pairStatus: PairStatus?,
    isCombining: Boolean,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onCombinePair: () -> Unit,
    onPrepareRetake: () -> Unit,
    onShowDeleteDialog: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(CompareHeaderHeight)
                .padding(horizontal = PairShotSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box {
            IconButton(
                onClick = { onMenuExpandedChange(true) },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "더보기",
                    modifier = Modifier.size(24.dp),
                )
            }
            PairShotTopMenu(
                expanded = menuExpanded,
                onDismissRequest = { onMenuExpandedChange(false) },
            ) {
                when (pairStatus) {
                    PairStatus.PAIRED -> {
                        PairShotTopMenuItem(
                            text = { Text("합성 이미지 생성") },
                            onClick = {
                                onMenuExpandedChange(false)
                                onCombinePair()
                            },
                            enabled = !isCombining,
                        )
                        PairShotTopMenuDivider()
                    }

                    else -> {}
                }
                PairShotTopMenuItem(
                    text = { Text("After 재촬영") },
                    onClick = {
                        onMenuExpandedChange(false)
                        onPrepareRetake()
                    },
                )
                PairShotTopMenuDivider()
                PairShotTopMenuItem(
                    text = { Text(text = "삭제", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        onMenuExpandedChange(false)
                        onShowDeleteDialog()
                    },
                )
            }
        }
    }
}
