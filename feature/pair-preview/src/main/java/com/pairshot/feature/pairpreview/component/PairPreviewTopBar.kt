package com.pairshot.feature.pairpreview.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pairshot.core.designsystem.PairShotSpacing

@Composable
fun PairPreviewTopBar(
    onClose: () -> Unit,
    onShareSelected: () -> Unit,
    onNavigateToAfterCamera: () -> Unit,
    onDeleteRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(PairShotSpacing.appBar)
                .padding(horizontal = PairShotSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "닫기",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        Box(modifier = Modifier.weight(1f))

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "더 보기",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "공유",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onShareSelected()
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "재촬영",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onNavigateToAfterCamera()
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "삭제",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDeleteRequested()
                    },
                )
            }
        }
    }
}
