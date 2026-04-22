package com.pairshot.core.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DeletePairConfirmDialog(
    pairCount: Int,
    combinedCount: Int,
    onDeleteAll: () -> Unit,
    onDeleteCombinedOnly: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (combinedCount > 0) {
        PairShotDialog(
            onDismissRequest = onDismiss,
            modifier = modifier,
            title = {
                Text(
                    text = "삭제 방식 선택",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Text(
                    text = "${pairCount}개 선택됨, ${combinedCount}개 합성본.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Row {
                    TextButton(onClick = onDeleteAll) {
                        Text(
                            text = "일괄 삭제",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    TextButton(onClick = onDeleteCombinedOnly) {
                        Text(
                            text = "합성본만",
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text(text = "취소")
                    }
                }
            },
        )
    } else {
        PairShotDialog(
            onDismissRequest = onDismiss,
            modifier = modifier,
            title = {
                Text(
                    text = "페어 삭제",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Text(
                    text = "${pairCount}개 페어를 삭제하시겠어요?",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onDeleteAll) {
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
}
