package com.pairshot.feature.compare.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pairshot.core.domain.model.PairStatus

@Composable
internal fun CompareHeader(
    pairNumber: Int,
    pairsSize: Int,
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
                .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(30.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text =
                if (pairNumber > 0) {
                    "$pairNumber/$pairsSize"
                } else {
                    ""
                },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
        )
        Box {
            IconButton(
                onClick = { onMenuExpandedChange(true) },
                modifier = Modifier.size(30.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "더보기",
                    modifier = Modifier.size(18.dp),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { onMenuExpandedChange(false) },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
            ) {
                when (pairStatus) {
                    PairStatus.PAIRED -> {
                        DropdownMenuItem(
                            text = { Text("합성 이미지 생성") },
                            onClick = {
                                onMenuExpandedChange(false)
                                onCombinePair()
                            },
                            enabled = !isCombining,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }

                    PairStatus.COMBINED -> {
                        DropdownMenuItem(
                            text = { Text("합성 결과 보기") },
                            onClick = {
                                onMenuExpandedChange(false)
                            },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }

                    else -> {}
                }
                DropdownMenuItem(
                    text = { Text("After 재촬영") },
                    onClick = {
                        onMenuExpandedChange(false)
                        onPrepareRetake()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                DropdownMenuItem(
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
