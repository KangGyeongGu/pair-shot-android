package com.pairshot.feature.pair.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pairshot.core.ui.component.PairShotDialog

@Composable
internal fun DeletePairsDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "선택 항목 삭제") },
        text = { Text(text = "${selectedCount}개 페어를 삭제하시겠습니까?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text(
                    text = "삭제",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소")
            }
        },
    )
}

@Composable
internal fun RenameProjectDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var newName by remember { mutableStateOf(currentName) }
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("프로젝트명 수정", style = MaterialTheme.typography.titleMedium) },
        text = {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors =
                    TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(newName)
                },
                enabled = newName.isNotBlank(),
            ) {
                Text("저장", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
internal fun DeleteProjectDialog(
    projectName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("프로젝트 삭제", style = MaterialTheme.typography.titleMedium) },
        text = {
            Text(
                "'$projectName' 프로젝트와 모든 사진이 삭제됩니다.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
internal fun DeleteWithCombinedDialog(
    pairCount: Int,
    combinedCount: Int,
    onDismiss: () -> Unit,
    onDeleteAll: () -> Unit,
    onDeleteCombinedOnly: () -> Unit,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("삭제 방식 선택", style = MaterialTheme.typography.titleMedium) },
        text = {
            Text(
                text = "${pairCount}개 선택됨, ${combinedCount}개 합성본.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDeleteAll) {
                    Text("일괄 삭제", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDeleteCombinedOnly) {
                    Text("합성본만", color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = onDismiss) {
                    Text("취소")
                }
            }
        },
    )
}
